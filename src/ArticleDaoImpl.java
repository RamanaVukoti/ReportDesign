/**
 * Copyright (C) 2008 GE Infra. All rights reserved
 *
 * @FileName ArticleDaoImpl.java
 * @Creation date: 10-Jan-2009
 * @version 1.0
 * @author Satyam
 */

package com.geinfra.gedrb.dao;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.model.SelectItem;

import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;

import com.geinfra.gedrb.cawc.integrator.CAWCDocument;
import com.geinfra.gedrb.data.ArticleAccessRequestData;
import com.geinfra.gedrb.data.ArticleAmendData;
import com.geinfra.gedrb.data.ArticleData;
import com.geinfra.gedrb.data.ArticleFolderData;
import com.geinfra.gedrb.data.DictionaryData;
import com.geinfra.gedrb.data.DocumentDetailsData;
import com.geinfra.gedrb.data.FileData;
import com.geinfra.gedrb.data.FolderMetaData;
import com.geinfra.gedrb.data.MyArticlesData;
import com.geinfra.gedrb.data.RequestGrantFolderArticleData;
import com.geinfra.gedrb.data.SecurityModelData;
import com.geinfra.gedrb.data.UserData;
import com.geinfra.gedrb.integrator.MappingException;
import com.geinfra.gedrb.services.UserServiceIfc;
import com.geinfra.gedrb.utils.ArticleConstants;
import com.geinfra.gedrb.utils.ChangeArticleClassificationAllProcedure;
import com.geinfra.gedrb.utils.ChangeArticleClassificationProcedure;
import com.geinfra.gedrb.utils.ChangeArticleOwnerProcedure;
import com.geinfra.gedrb.utils.CheckTaggingProcedure;
import com.geinfra.gedrb.utils.CreateArticleProcedure;
import com.geinfra.gedrb.utils.DRBCommonException;
import com.geinfra.gedrb.utils.DRBQueryConstants;
import com.geinfra.gedrb.utils.DRBUtils;
import com.geinfra.gedrb.utils.ECTagConstants;
import com.geinfra.gedrb.utils.FolderConstants;
import com.geinfra.gedrb.utils.LibraryConstants;
import com.geinfra.gedrb.utils.MagicNumberConstants;
import com.geinfra.gedrb.utils.MoveArticleProcedure;
import com.geinfra.gedrb.utils.NamedTaggerProcedure;
import com.geinfra.gedrb.utils.ODARRCAWCConstants;
import com.geinfra.gedrb.utils.ReportConstants;
import com.geinfra.gedrb.utils.SearchConstants;
import com.geinfra.gedrb.utils.SendMailUtil;
import com.geinfra.gedrb.utils.UpdateArticleStateProcedure;
import com.geinfra.gedrb.utils.ViewArticleProc;

/**
 * @author nk32998
 */
public class ArticleDaoImpl extends SimpleJdbcDaoSupport implements
		ArticleDaoIfc, MagicNumberConstants {

	ViewArticleProc viewArticleProc;

	/*********************** Start of Methods Added by Aishwarya for Audit Folder *************************/

	static class getCountFlag implements ParameterizedRowMapper<String> {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String countValue = rs.getString("COUNT");
			return countValue;
		}
	}

	private static final Logger LOG = Logger.getLogger(ArticleDaoImpl.class);

	private static ParameterizedRowMapper<FileData> fileDownloadMapper = new ParameterizedRowMapper<FileData>() {
		public FileData mapRow(ResultSet rs, int rowNum) throws SQLException {
			FileData fileData = new FileData();
			fileData.setDocumentName(rs
					.getString(ReportConstants.DOCUMENT_NAME));
			fileData.setLocationPath(rs.getString(ReportConstants.NETWORK_PATH));
			return fileData;
		}
	};
	/***************************************** End:Added by Payel *****************************************************/

	private static ParameterizedRowMapper<MyArticlesData> userViewedMapper = new ParameterizedRowMapper<MyArticlesData>() {
		public MyArticlesData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			MyArticlesData articleData = new MyArticlesData();
			articleData.setCount(rs.getInt(ReportConstants.COUNT));
			articleData.setDocSeqId(rs.getInt(ReportConstants.APP_DATA_PK_ID));
			return articleData;
		}
	};

	private static ParameterizedRowMapper<FileData> fileDataMapper = new ParameterizedRowMapper<FileData>() {

		public FileData mapRow(ResultSet rs, int rowNum) throws SQLException {
			FileData fileData = new FileData();
			fileData.setFileChecked(false);
			fileData.setDocumentSeqId(rs
					.getString(ReportConstants.DOCUMENT_SEQ_ID));
			fileData.setDocumentName(rs
					.getString(ReportConstants.DOCUMENT_NAME));
			fileData.setDocumentTitle(rs
					.getString(ReportConstants.DOCUMENT_TITLE));
			fileData.setDocDescription(rs
					.getString(ReportConstants.DOCUMENT_DESCRIPTION));
			fileData.setLocationPath(rs.getString(ReportConstants.NETWORK_PATH));
			return fileData;
		}
	};

	private static ParameterizedRowMapper<String> docClassifcationMapper = new ParameterizedRowMapper<String>() {
		public String mapRow(ResultSet rs1, int rowNum) throws SQLException {
			String docClassName = ArticleConstants.EMPTY;
			docClassName = (DRBUtils.checkNullVal((String) rs1
					.getString(FolderConstants.DATA_CLASS_NAME)));
			return docClassName;

		}
	};

	private static ParameterizedRowMapper<ArticleAmendData> historyMapper = new ParameterizedRowMapper<ArticleAmendData>() {
		public ArticleAmendData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			ArticleAmendData metaData = new ArticleAmendData();
			metaData.setUserID(rs.getString(FolderConstants.USER_ID));
			metaData.setChangedattname(rs.getString("CHANGED_ATTRIBUTE_NAME"));
			metaData.setOldValue(rs.getString("OLD_VALUE"));
			metaData.setNewValue(rs.getString("NEW_VALUE"));
			if (DRBUtils.isEmpty(metaData.getOldValue())) {
				metaData.setOldValue(ArticleConstants.NONE_FOR_HIST);
			}
			if (DRBUtils.isEmpty(metaData.getNewValue())) {
				metaData.setNewValue(ArticleConstants.NONE_FOR_HIST);
			}
			if (!DRBUtils.isEmpty(metaData.getOldValue())) {
				if (metaData.getOldValue()
						.equalsIgnoreCase(FolderConstants.UNK)) {
					metaData.setOldValue(FolderConstants.FOLDER_UNKNOWN);
				}
			}
			if (!DRBUtils.isEmpty(metaData.getNewValue())) {
				if (metaData.getNewValue()
						.equalsIgnoreCase(FolderConstants.UNK)) {
					metaData.setNewValue(FolderConstants.FOLDER_UNKNOWN);
				}
			}
			String firstName = rs.getString("CHANGE_USER_FIRST_NAME");
			String lastName = rs.getString("CHANGE_USER_LAST_NAME");
			metaData.setFullName(DRBUtils.setPersonnelFullName(lastName,
					firstName));
			metaData.setCreationDate(rs.getTimestamp("LAST_UPDATE_DATE"));
			return metaData;
		}
	};

	private static ParameterizedRowMapper<ArticleData> articleMapper = new ParameterizedRowMapper<ArticleData>() {
		public ArticleData mapRow(ResultSet rs, int rowNum) throws SQLException {
			ArticleData metaData = new ArticleData();
			metaData.setArticleTitle(DRBUtils.checkNullVal(rs
					.getString("ARTICLE_TITLE")));
			String firstName = rs.getString("FIRST_NAME");
			String lastName = rs.getString("LAST_NAME");
			metaData.setArticleOwner(DRBUtils.setEmailPersonnelFullName(
					lastName, firstName));
			return metaData;
		}
	};

	private static ParameterizedRowMapper<FileData> fileDataDownloadMapper = new ParameterizedRowMapper<FileData>() {

		public FileData mapRow(ResultSet rs, int rowNum) throws SQLException {
			FileData fileData = new FileData();
			fileData.setFileChecked(false);
			fileData.setDocumentSeqId(rs
					.getString(ReportConstants.DOCUMENT_SEQ_ID));
			fileData.setDocumentName(rs
					.getString(ReportConstants.DOCUMENT_NAME));
			fileData.setDocumentTitle(rs
					.getString(ReportConstants.DOCUMENT_TITLE));
			fileData.setDocDescription(rs
					.getString(ReportConstants.DOCUMENT_DESCRIPTION));
			fileData.setLocationPath(rs.getString(ReportConstants.NETWORK_PATH));
			fileData.setAppDataPk(rs.getString(ReportConstants.APP_DATA_PK_ID));

			// FUTURE CODE
			/*
			 * if("Y".equals(rs.getString("ARCHIVE_IND"))){
			 * fileData.setFileArchived(true); }else{
			 * fileData.setFileArchived(false); }
			 */
			return fileData;
		}
	};

	/***************************************** Start:Added by Payel *****************************************************/
	/**
	 * @purpose Mapper for retrieving attachments List
	 */
	private static ParameterizedRowMapper<ArticleAmendData> attachmentMapper = new ParameterizedRowMapper<ArticleAmendData>() {
		public ArticleAmendData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			ArticleAmendData attachmentData = new ArticleAmendData();
			attachmentData.setFileChecked(false);
			attachmentData.setAttachmentSeqID(rs
					.getInt(ReportConstants.DOCUMENT_SEQ_ID));
			attachmentData.setAttachmentTitle(rs
					.getString(ReportConstants.DOCUMENT_TITLE));
			attachmentData.setAttachmentName(rs
					.getString(ReportConstants.DOCUMENT_NAME));
			attachmentData.setLocationPath(rs
					.getString(ReportConstants.NETWORK_PATH));
			if (attachmentData.getAttachmentTitle().endsWith(".htm")
					|| attachmentData.getAttachmentTitle().endsWith(".html")) {
				attachmentData.setHtmlType("Y");
			} else {
				attachmentData.setHtmlType("N");
			}
			File newFile = new File(attachmentData.getLocationPath()
					+ attachmentData.getAttachmentName());
			if ((newFile.length() / ArticleConstants.ONE_THOUSAND) == 0) {
				attachmentData.setAttachmentSize(1);
			} else {
				attachmentData.setAttachmentSize(newFile.length()
						/ ArticleConstants.ONE_THOUSAND);
			}
			long fileSize = rs.getString("FILE_SIZE") == null ? 0 : Long
					.valueOf(rs.getString("FILE_SIZE"));
			if ((fileSize / ArticleConstants.ONE_THOUSAND) == 0) {
				attachmentData.setAttachmentSize(1);
			} else {
				attachmentData.setAttachmentSize(fileSize
						/ ArticleConstants.ONE_THOUSAND);
			}
			long MEGABYTE = 1024L * 1024L;
			if ((fileSize / MEGABYTE) >= 75) {
				attachmentData.setBulkDownload(false);
			} else {
				attachmentData.setBulkDownload(true);
			}

			// * Below Archival Ind Code needs to be uncommented when needed
			/*
			 * if("Y".equals(rs.getString("ARCHIVE_IND"))){
			 * attachmentData.setArchiveInd(true); }else{
			 * attachmentData.setArchiveInd(false); }
			 */
			return attachmentData;
		}
	};

	/**
	 * @purpose mapper class for finding whether the article is older than 12
	 *          months or not
	 */
	private static ParameterizedRowMapper<String> oldArticleCountMapper = new ParameterizedRowMapper<String>() {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String articleOldFlg = null;
			articleOldFlg = rs.getString(DRBQueryConstants.COUNT);
			return articleOldFlg;
		}
	};

	/**
	 * @param artSeqId
	 *            Start:Added by Pradeep for Enhancement No: 68
	 * @throws DRBCommonException
	 */

	private static ParameterizedRowMapper<FolderMetaData> bookmarkslist = new ParameterizedRowMapper<FolderMetaData>() {
		public FolderMetaData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			FolderMetaData metaData = new FolderMetaData();
			metaData.setFolderCode(rs.getString(FolderConstants.FOLDER_CODE));
			metaData.setFolderTitle(rs.getString(FolderConstants.FOLDER_TITLE));
			metaData.setFolderPath(rs.getString(FolderConstants.FOLDER_PATH));
			metaData.setArticleFolderSeqId(rs
					.getString(FolderConstants.ARTICLE_FOLDER_SEQ_ID));
			metaData.setAssetType(rs.getString(FolderConstants.ASSET_TYPE));
			metaData.setArticleStateName(rs
					.getString(FolderConstants.STATE_NAME)); // Added By Santosh
																// on 12JAN11
			metaData.setLockInd(rs.getString(FolderConstants.LOCK_IND));// Added
																		// by
																		// Pradeep
																		// on
																		// 24/01/2011
																		// for
																		// Defect
																		// Id:
																		// 1453
			metaData.setContentInd(rs.getString(FolderConstants.CONTENT_IND));// Added
																				// by
																				// Pradeep
																				// on
																				// 24/01/2011
																				// for
																				// Defect
																				// Id:
																				// 1453
			metaData.setObjectEcTag(rs
					.getString(ReportConstants.ARTICLE_OBJ_EC_TAG));
			metaData.setObjectDocumentClass(rs
					.getString(ReportConstants.ARTICLE_DOC_CLASS));
			return metaData;
		}
	};

	/*********************** End of Methods Added by Aishwarya for Audit Folder *************************/

	private NamedTaggerProcedure _namedTaggerProcedure = new NamedTaggerProcedure();// Code
																					// Review
																					// Changed
																					// By
																					// Naresh
																					// on
																					// 22DEC10
	private JavaMailSenderImpl _mailSend;
	private SendMailUtil _mailBean;
	private UserServiceIfc _userServiceObj;
	private CreateArticleProcedure _createArticlePrc;
	private ChangeArticleOwnerProcedure _changeArticleOwnerPrc;
	private ChangeArticleClassificationProcedure _changeArticleClassificationPrc;
	// Added by Pradeep for Document Classification on 02-12-2010
	private MoveArticleProcedure _moveArticlePrc;
	private UpdateArticleStateProcedure _updateArticleStatePrc;
	private CheckTaggingProcedure _checkTaggingProcedure;
	private SimpleDateFormat simple = new SimpleDateFormat("MM/dd/yyyy");
	private ChangeArticleClassificationAllProcedure _changeArticleClassificationAllPrc;

	private String _titleOld = "";// Added by Pradeep on 28/01/2011 for Defect
									// Id: 1455

	/**
	 * @return the updateArticleStatePrc
	 */
	public UpdateArticleStateProcedure getUpdateArticleStatePrc() {
		return _updateArticleStatePrc;
	}

	/**
	 * @param updateArticleStatePrc
	 *            the updateArticleStatePrc to set
	 */
	public void setUpdateArticleStatePrc(
			UpdateArticleStateProcedure updateArticleStatePrc) {
		this._updateArticleStatePrc = updateArticleStatePrc;
	}

	/**
	 * @return the _changeArticleOwnerPrc
	 */
	public ChangeArticleOwnerProcedure getChangeArticleOwnerPrc() {
		return _changeArticleOwnerPrc;
	}

	/**
	 * @param _changeArticleOwnerPrc
	 *            the changeArticleOwnerPrc to set
	 */
	public void setChangeArticleOwnerPrc(
			ChangeArticleOwnerProcedure changeArticleOwnerPrc) {
		this._changeArticleOwnerPrc = changeArticleOwnerPrc;
	}

	/**
	 * @param reqID
	 * @return String
	 */
	public ArticleAccessRequestData getReqDetails(String reqID) {

		final ArticleAccessRequestData reqData = new ArticleAccessRequestData();

		getSimpleJdbcTemplate().query(DRBQueryConstants.REQ_DETAILS_BY_REQID,
				new ParameterizedRowMapper<ArticleAccessRequestData>() {
					public ArticleAccessRequestData mapRow(ResultSet rs,
							int rowNum) throws SQLException {

						reqData.setArtSeqID(rs.getString(1));
						reqData.setArticleNumber(rs.getString(2));
						reqData.setArticleName(rs.getString(3));
						reqData.setOwnerOfArticle(rs.getString(4));
						reqData.setArticleLocation(rs.getString(5));
						reqData.setRequestedTo(rs.getString(6));
						reqData.setExpiryDate(rs.getDate(7));
						reqData.setReasonToAccess(rs.getString(8));
						reqData.setRequestedBy(rs.getString(9));
						reqData.setReqTypeID(rs.getString(10));

						return null;
					}
				}, reqID);

		// getting requestor's company;

		List<String> reqCompDetails = getSimpleJdbcTemplate().query(
				DRBQueryConstants.REQSTOR_COMPANY_BY_ID,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						return rs.getString(1);

					}
				}, reqData.getRequestedBy());

		if (reqCompDetails.size() > 1) {
			reqData.setRequestorCompany(reqCompDetails.get(0));
			reqData.setRequestorEmpType(reqCompDetails.get(1));
		} else
			reqData.setRequestorCompany(null);

		return reqData;
	}

	/**
	 * @return String
	 */
	@Transactional
	public String grantArticleReq(ArticleAccessRequestData reqData)
			throws DRBCommonException {
		// Log.info("%%%%%%%%%%%%%%%% in Dao granting article access %%%%%%%%%%%%%%%%");
		String result = "articleAccessGranted";
		String resultF = "grantArticleAccessFailed";
		int rowsUpdated1 = 0;
		int rowsUpdated2 = 0;
		int rowsUpdated3 = 0;
		rowsUpdated1 = getSimpleJdbcTemplate().update(
				DRBQueryConstants.UPDATE_REQ_EVENT_TAB,
				reqData.getOwnerOfArticlename(), reqData.getReqSeqID());

		rowsUpdated2 = getSimpleJdbcTemplate().update(
				DRBQueryConstants.UPDATE_REQEVENT_USER_TAB,
				reqData.getExpiryDate(), reqData.getReqSeqID(),
				reqData.getRequestedBy(), reqData.getOwnerOfArticlename(),
				reqData.getReqSeqID());

		int rowCount = 0;
		rowCount = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.CHECK_IF_ENTRY_EXISTING,
				reqData.getRequestedBy(), reqData.getArtSeqID());

		if (rowCount != 0) {
			rowsUpdated3 = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_DRB_USER_DATA_ACCESS_GRNT_ART_ACC,
					reqData.getExpiryDate(), reqData.getOwnerOfArticlename(),
					reqData.getRequestedBy(), reqData.getArtSeqID());
		} else {
			rowsUpdated3 = getSimpleJdbcTemplate().update(
					DRBQueryConstants.INSERT_DRB_USER_DATA_ACCESS_GRNT_ART_ACC,
					reqData.getArtSeqID(), reqData.getRequestedBy(),
					reqData.getExpiryDate(), reqData.getOwnerOfArticlename(),
					reqData.getOwnerOfArticlename());
		}

		if (rowsUpdated1 + rowsUpdated2 + rowsUpdated3 == 3)
			return result;
		else
			return resultF;
	}

	// added by santosh on 10 dec for ge doc start ASSIGN_ARTICLE_DOC_CHANGE
	public void getChangedGedoc(String articleSeqId) throws DRBCommonException {

		getSimpleJdbcTemplate().update(
				DRBQueryConstants.ASSIGN_ARTICLE_DOC_CHANGE, articleSeqId);
		// LOG.info("***getChangedGedoc successfully****in daoimpl***");

	}

	public void getPITagToNo(String articleSeqId) throws DRBCommonException {

		getSimpleJdbcTemplate().update(
				DRBQueryConstants.ASSIGN_ARTICLE_DOC_CHANGE_IP_TAG,
				articleSeqId);
		// LOG.info("***getChangedGedoc successfully****in daoimpl***");

	}

	// /added by santosh ends on 10 dec
	/**
	 * @return String
	 */
	public String rejectArticleReq(final ArticleAccessRequestData reqData)
			throws DRBCommonException {
		String result = "rejectArticleAccess";
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.UPDATE_REQ_EVENT_TAB_REJ,
				reqData.getOwnerOfArticlename(),
				reqData.getRejectionComments(), reqData.getReqSeqID());
		return result;
	}

	// added by santosh on 30 nov strat
	/**
	 * @return String
	 */
	public String rejectArticleReqManage(final ArticleAccessRequestData reqData)
			throws DRBCommonException {
		String result = "rejectArticleAccessManage";
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.UPDATE_REQ_EVENT_TAB_REJ,
				reqData.getOwnerOfArticlename(),
				reqData.getRejectionComments(), reqData.getReqSeqID());
		// LOG.info("*****article adoimpl success*****");
		return result;
	}

	/**
	 * @return the userServiceObj
	 */
	public UserServiceIfc getUserServiceObj() {
		return _userServiceObj;
	}

	/**
	 * @param userServiceObj
	 *            the userServiceObj to set
	 */
	public void setUserServiceObj(UserServiceIfc userServiceObj) {
		this._userServiceObj = userServiceObj;
	}

	/**
	 * @param requestorId
	 * @param articleSeqId
	 * @return String
	 */
	public String checkIfAlreadyRequested(String requestorId,
			String articleSeqId) throws DRBCommonException {
		String errMsg = null;
		try {
			List<Integer> reqSeqId;
			ParameterizedRowMapper<Integer> getCount = new ParameterizedRowMapper<Integer>() {
				public Integer mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					int reqVal = rs.getInt(1);
					return reqVal;
				}
			};
			reqSeqId = getSimpleJdbcTemplate().query(
					DRBQueryConstants.CHECK_EXISTING_REQUEST, getCount,
					articleSeqId, requestorId);

			if (reqSeqId.get(0) != 0) {
				errMsg = "You have applied Named access for this Article. Request is already in process";
			} else {
				errMsg = "notvalidate";
			}
		} catch (DataRetrievalFailureException e) {
			LOG.error("An Error has occured in ArticleDaoImp :: checkIfAlreadyRequested >>> "
					+ e.getMessage());
		}
		return errMsg;
	}

	/**
	 * @param artdata
	 * @return String
	 */
	@Transactional
	public String submitArticleAccessRequest(ArticleData artdata)
			throws DRBCommonException {
		ArticleData articleDataReqArtAccess = artdata;
		int rowsinserted1 = 0;
		int rowsinserted2 = 0;
		// getting request sequence id
		int reqSeqID = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_REQ_SEQ_ID_NEXT_VALS);
		// inserting into DRB_REQUEST_EVENT

		rowsinserted1 = getSimpleJdbcTemplate().update(
				DRBQueryConstants.INSERT_INTO_DRBE_REQUEST_EVENT, reqSeqID,
				articleDataReqArtAccess.getArticleSeqid(),
				articleDataReqArtAccess.getAccessRequestedByID(),
				articleDataReqArtAccess.getArticleOwnerSSO(),
				articleDataReqArtAccess.getReasonToAccess(),
				articleDataReqArtAccess.getAccessRequsetedByName(),
				articleDataReqArtAccess.getAccessRequsetedByName(),
				articleDataReqArtAccess.getArticleSeqid(),
				articleDataReqArtAccess.getArticleSeqid());
		// inserting into DRB_REQEVENT_USER
		rowsinserted2 = getSimpleJdbcTemplate().update(
				DRBQueryConstants.INSERT_INTO_DRB_REQEVEN_USER, reqSeqID,
				articleDataReqArtAccess.getAccessRequestedByID(),
				articleDataReqArtAccess.getExpiryDateByRequestor(),
				articleDataReqArtAccess.getAccessRequsetedByName(),
				articleDataReqArtAccess.getAccessRequsetedByName());
		// Log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> getting article code ");
		String artCode = (String) getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.ARTICLE_CODE_BY_SEQ_ID,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						final String articleCode = rs.getString(1);
						return articleCode;
					}
				}, articleDataReqArtAccess.getArticleSeqid());

		// Log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> article code : " + artCode);

		if ((rowsinserted1 + rowsinserted2) == 2)
			return "toMyRequestsSuccess" + "~" + artCode;
		else
			return "toMyRequestsFailure";

	}

	/**
	 * @return mailBean
	 */
	public SendMailUtil getMailBean() {
		return _mailBean;
	}

	/**
	 * @param mailBean
	 */
	public void setMailBean(SendMailUtil mailBean) {
		this._mailBean = mailBean;
	}

	/**
	 * @return mailSend
	 */
	public JavaMailSenderImpl getMailSend() {
		return _mailSend;
	}

	/**
	 * @param mailSend
	 */
	public void setMailSend(JavaMailSenderImpl mailSend) {
		this._mailSend = mailSend;
	}

	/**
	 * This method is used to Approve the Article
	 * 
	 * @param articleNo
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String approveArticle(ArticleData articleData, String revComments,
			String status) throws DRBCommonException {
		String toUpdate = isArticleMetaDataEdited(articleData.getArticleSeqid());
		String metaDataField = toUpdate;
		String APPROVE_ARTICLE_BY_SEQ_ID = null;
		String getReviewComments = null;
		List<String> reviewerComments = null;
		String prevReviewComments = null;
		String oldTitleValue = articleData.getArticleOldTitle();// Added by
																// Pradeep on
																// 28/01/2011
																// for Defect
																// Id: 1455
		String newTitleValue = articleData.getArticleTitle();// Added by Pradeep
																// on 28/01/2011
																// for Defect
																// Id: 1455
		// Start: Added by Pradeep on 28/01/2011 for Defect Id: 1455
		if (articleData.getArticleOldTitle() != null
				&& !((oldTitleValue.trim()).equalsIgnoreCase(newTitleValue
						.trim()))) {
			this._titleOld = articleData.getArticleOldTitle();
			/* LOG.info("------ Title Old---------"+_titleOld); */
		}
		// End: Added by Pradeep on 28/01/2011 for Defect Id: 1455
		/*
		 * Avoid reassigning parameters Naresh : Added on 30DEC10 as per SQDB
		 * Results
		 */
		String reviewComments = revComments;

		/* Start: added for sending comments in mail when no selected */
		String primaryRevCommentsTotal = articleData.getReviewerComments();
		String additionalRevCommentsTotal = articleData
				.getAdditionalReviewerComments();
		/* End: added for sending comments in mail when no selected */

		/*
		 * There will be no change in ARTICLE_WF_STATE_SEQ_ID if Review when
		 * REVIEWER_METADATA_EDIT_IND='Y', But it go to Owner for approval if he
		 * approve(s) Article, Article will be issued and
		 * REVIEWER_METADATA_EDIT_IND='N' else,REVIEWER_METADATA_EDIT_IND='N'
		 * and ARTICLE_WF_STATE_SEQ_ID will change to draft.
		 */

		// Defaut Name
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String ReviewName = userObj.getUserLastName() + ", "
				+ userObj.getUserFirstName();
		// Mythili : Get the attachment verifcation score
		List<String> theAttchmentLs = getVerificationScoreForRevision(articleData
				.getArticleSeqid());
		boolean verificationPending = false;
		if (!DRBUtils.isEmptyList(theAttchmentLs)) {
			for (int i = 0; i < theAttchmentLs.size(); i++) {
				String theAttachScore = theAttchmentLs.get(i);
				String theArr[] = theAttachScore.split("#");
				if (theArr != null && theArr.length > 1) {
					if (("2".equals(theArr[1])) || ("3".equals(theArr[1]))) {
						verificationPending = true;
						break;
					}
				}
			}
		}
		if (status.equals("Review Article")) {
			// Log.info("Reviewer Approve");
			if ("N".equalsIgnoreCase(updationOnReviewStatus(toUpdate,
					articleData))) {
				// Log.info("Not Edited");/* not edited */
				String userid = "";
				// have to know role of reviewer[NOTE:true-->default review] if
				if (userObj.getUserSSOID().equalsIgnoreCase(
						articleData.getReviewerid())) {
					APPROVE_ARTICLE_BY_SEQ_ID = DRBQueryConstants.PRIMARY_REVIEWER_APPROVAL;
					userid = articleData.getReviewerid();
					articleData
							.setReviewerDisposition(ArticleConstants.APPROVED);
				} else {
					APPROVE_ARTICLE_BY_SEQ_ID = DRBQueryConstants.ADDITIONAL_REVIEWER_APPROVAL;
					userid = articleData.getAdditionalReviewerId();
					articleData
							.setAdditionalReviewerDisposition(ArticleConstants.APPROVED);

				}
				getSimpleJdbcTemplate().update(APPROVE_ARTICLE_BY_SEQ_ID,
						ArticleConstants.APPROVED, ReviewName, "",
						articleData.getArticleSeqid());
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_REVIEWER_REQ_EVENT_TAB,
						ArticleConstants.APPROVED, ArticleConstants.CHAR_N,
						ReviewName, articleData.getArticleSeqid(), userid,
						"Review Article");
				if (!DRBUtils.isEmpty(articleData.getReviewerDisposition())
						&& articleData.getReviewerDisposition()
								.equalsIgnoreCase(ArticleConstants.APPROVED)) {
					articleData
							.setChangedAttributeName("Primary reviewer disposition");
				}
				if (!DRBUtils.isEmpty(articleData
						.getAdditionalReviewerDisposition())
						&& articleData.getAdditionalReviewerDisposition()
								.equalsIgnoreCase(ArticleConstants.APPROVED)) {
					articleData
							.setChangedAttributeName("Secondary reviewer disposition");
				}
				articleData.setOldValue("");
				articleData.setNewValue(ArticleConstants.APPROVED);
				// Log.info("status here while approval 11111111" + histStatus);

				// End of History capture
			} else {
				// TODO : MYTHILI //verification logic
				// Log.info("Edited");
				/*
				 * When Article is Edited and if Reviewer(s) then state will not
				 * change but an Article approval will move to Article Owner.
				 */
				String userid = "";
				if (userObj.getUserSSOID().equalsIgnoreCase(
						articleData.getReviewerid())) {
					APPROVE_ARTICLE_BY_SEQ_ID = DRBQueryConstants.PRIMARY_REVIEWER_APPROVAL_EDIT_MODE;
					getReviewComments = DRBQueryConstants.GET_REVIEWER_COMMENTS;
					userid = articleData.getReviewerid();
					// otherUserid = articleData.getAdditionalReviewerid();
					articleData.setReviewerComments(reviewComments);
					articleData
							.setReviewerDisposition(ArticleConstants.APPROVED);

				} else {
					APPROVE_ARTICLE_BY_SEQ_ID = DRBQueryConstants.ADDITIONAL_REVIEWER_APPROVAL_EDIT_MODE;
					getReviewComments = DRBQueryConstants.GET_ADDTIONAL_REVIEWER_COMMENTS;
					userid = articleData.getAdditionalReviewerId();
					articleData.setAdditionalReviewerComments(reviewComments);
					articleData
							.setAdditionalReviewerDisposition(ArticleConstants.APPROVED);

				}
				reviewerComments = getSimpleJdbcTemplate().query(
						getReviewComments,
						new ParameterizedRowMapper<String>() {
							public String mapRow(ResultSet rs, int arg1)
									throws SQLException {
								String edited = rs.getString("REVIEW_COMMENTS");
								return edited;
							}
						}, articleData.getArticleSeqid());
				if (reviewerComments != null && reviewerComments.size() > 0) {
					prevReviewComments = reviewerComments.get(0);
				}
				if (prevReviewComments != null
						&& !prevReviewComments.equals("")) {
					reviewComments = prevReviewComments
							+ "\n\n --------------------\n\n" + reviewComments;
				}
				getSimpleJdbcTemplate().update(APPROVE_ARTICLE_BY_SEQ_ID,
						ArticleConstants.APPROVED, ReviewName, reviewComments,
						articleData.getArticleSeqid());
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_REVIEWER_REQ_EVENT_TAB,
						ArticleConstants.APPROVED, ArticleConstants.CHAR_N,
						ReviewName, articleData.getArticleSeqid(), userid,
						"Review Article");

				if (userObj.getUserSSOID().equalsIgnoreCase(
						articleData.getReviewerid())) {
					primaryRevCommentsTotal = reviewComments;
				} else {
					additionalRevCommentsTotal = reviewComments;
				}
				// Added by Devi for History capture while approving
				/*
				 * //Log.info("2222222222222the primary reviewer disposition " +
				 * articleData.getReviewerDisposition());
				 * //Log.info("2222222222the secondary reviewer disposition " +
				 * articleData.getAdditionalReviewerDisposition());
				 */
				if (!DRBUtils.isEmpty(articleData.getReviewerDisposition())
						&& articleData.getReviewerDisposition()
								.equalsIgnoreCase(ArticleConstants.APPROVED)) {
					articleData
							.setChangedAttributeName("Primary reviewer disposition");
				}
				if (!DRBUtils.isEmpty(articleData
						.getAdditionalReviewerDisposition())
						&& articleData.getAdditionalReviewerDisposition()
								.equalsIgnoreCase(ArticleConstants.APPROVED)) {
					articleData
							.setChangedAttributeName("Secondary reviewer disposition");
				}
				articleData.setOldValue("");
				articleData.setNewValue(ArticleConstants.APPROVED);
				// End of History capture
			}
			// Added by Avinash for 5.4.5 Article review status
			if ((!("N".equalsIgnoreCase(articleData.getReviewMetaDatInd())))
					|| (metaDataField.equals("P"))
					|| (metaDataField.equals("A"))) {
				if (((articleData.getAdditionalReviewerId() != null) && (!(articleData
						.getAdditionalReviewerId().equals(""))))
						&& ((articleData.getAdditionalReviewer() != null) && (!(articleData
								.getAdditionalReviewer().equals(""))))) {
					if ((articleData.getReviewerDisposition()
							.equals(ArticleConstants.APPROVED))
							&& (articleData.getAdditionalReviewerDisposition()
									.equals(ArticleConstants.APPROVED))) {
						getSimpleJdbcTemplate().update(
								DRBQueryConstants.UPDATE_REVIEW_STATE,
								ArticleConstants.APPROVED_WITH_EDITS,
								articleData.getArticleSeqid());

					} else {
						getSimpleJdbcTemplate().update(
								DRBQueryConstants.UPDATE_REVIEW_STATE,
								ArticleConstants.IN_REVIEW,
								articleData.getArticleSeqid());
					}
				} else {
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.UPDATE_REVIEW_STATE,
							ArticleConstants.APPROVED_WITH_EDITS,
							articleData.getArticleSeqid());
				}
			}

			String result = getDispositions(articleData.getArticleSeqid());
			// Start: Added for asking user to submit the comment to
			// ArticleOwner
			boolean articleEdited = true;
			if (result != null && !articleData.isRequestToOwner()) {
				if ((ArticleConstants.NO_STRING).equalsIgnoreCase(result)) {
					String query = DRBQueryConstants.UPDATE_ARTICLE_EDIT_STATE_WOMD_EDIT;
					getSimpleJdbcTemplate().update(query, "N",
							articleData.getArticleSeqid());
				} else {
					if (metaDataField.equalsIgnoreCase("Y")) {
						String articlEditInd = null;
						if (userObj.getUserSSOID().equalsIgnoreCase(
								articleData.getReviewerid())) {
							articlEditInd = "A";
						} else {
							articlEditInd = "P";
						}
						String query = DRBQueryConstants.UPDATE_ARTICLE_EDIT_STATE_WOMD_EDIT;
						getSimpleJdbcTemplate().update(query, articlEditInd,
								articleData.getArticleSeqid());
						metaDataField = articlEditInd;
						articleEdited = true;
					} else if (!articleData.isEditedByOtherReviewer()) {
						articleEdited = false;
					}
				}
			} else if (result.split(ArticleConstants.TILDA)[0]
					.equalsIgnoreCase(ArticleConstants.STATE_ISSUED)) {
				List<String> changedData = saveModifiedData(articleData
						.getArticleSeqid());
				if (changedData.size() > 0) {
					articleEdited = false;
					result = ArticleConstants.STATE_ISSUEDWITHEDITS
							+ ArticleConstants.TILDA
							+ result.split(ArticleConstants.TILDA)[1];
				}
			}
			// End: Added for asking user to submit the comment to ArticleOwner
			if (!"NO".equalsIgnoreCase(result)) {
				if (result != null
						&& (result.split("~")[0].equalsIgnoreCase("Issued") || result
								.split("~")[0].equalsIgnoreCase("Draft"))) {
					if (result.split("~")[0].equalsIgnoreCase("Issued")) {
						if (!verificationPending) {
							getSimpleJdbcTemplate()
									.update(DRBQueryConstants.ARTICLE_REVIEW_STATE_TRANCE_TO_ISSUE,
											"", "", ArticleConstants.CHAR_N,
											result.split("~")[0],
											ArticleConstants.APPROVED,
											articleData.getArticleSeqid());
						} else {
							getSimpleJdbcTemplate()
									.update(DRBQueryConstants.UPDATE_REVIEW_STATE,
											ArticleConstants.APPROVED_WITH_VERIFICATION_PENDING,
											articleData.getArticleSeqid());
						}
					} else {
						getSimpleJdbcTemplate()
								.update(DRBQueryConstants.ARTICLE_REVIEW_STATE_TRANCE_TO_DRAFT,
										result.split("~")[0],
										ArticleConstants.REJECTED,
										articleData.getArticleSeqid());
					}

					if ((result.split("~")[0].equalsIgnoreCase("Issued") && !verificationPending)
							|| !result.split("~")[0].equalsIgnoreCase("Issued")) {
						// Added by devi for capturing to history
						articleData
								.setChangedAttributeName(result.split("~")[0]);
						articleData.setOldValue(articleData.getArticleState());
						articleData.setNewValue(result.split("~")[0]);
						captureArticlesHistory(articleData);
					}

					if (result.split("~")[0].equalsIgnoreCase("Issued")) {
						/**** lasted updated **********/

						if (!verificationPending) {

							List<String> checkoutArticle = getSimpleJdbcTemplate()
									.query("SELECT STATE_NAME,ARTICLE_SEQ_ID FROM CAWC.DRB_ARTICLE A,CAWC.DRB_WF_STATE S WHERE "
											+ "ARTICLE_SEQ_ID=(SELECT REVISION_ARTICLE_SEQ_ID FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID=?) "
											+ " AND A.ARTICLE_WF_STATE_SEQ_ID=S.WF_STATE_SEQ_ID",
											new ParameterizedRowMapper<String>() {
												public String mapRow(
														ResultSet rs, int arg1)
														throws SQLException {
													return rs
															.getString("STATE_NAME")
															+ "~"
															+ rs.getString("ARTICLE_SEQ_ID");
												}
											}, articleData.getArticleSeqid());
							UserData ownerUserObj = (UserData) (DRBUtils
									.getServletSession(false)
									.getAttribute(ArticleConstants.SESSION_USER_DATA));
							String userName = ownerUserObj.getUserLastName()
									+ ArticleConstants.EDRB_SEPARATOR
									+ ownerUserObj.getUserFirstName();
							int updatation = getSimpleJdbcTemplate()
									.update(DRBQueryConstants.ARTICLE_REVISE_ARTICLE_PROMOTE_ARTICLE_QUERY,
											new Object[] {
													userName,
													"N",
													articleData
															.getArticleSeqid() });
							if (updatation == 1) {

								if (checkoutArticle != null
										&& checkoutArticle.size() > 0) {
									// Log.info("in the check out article case "
									// + result.split("~")[0]);
									articleData.setChangedAttributeName(result
											.split("~")[0]);
									articleData.setOldValue(checkoutArticle
											.get(0).split("~")[0]);
									articleData
											.setNewValue(result.split("~")[0]);
									articleData.setArticleSeqid(checkoutArticle
											.get(0).split("~")[1]);
									captureArticlesHistory(articleData);

								}

							}

							sendArticleTaskMail(
									articleData,
									articleData.getArticleTitle(),
									articleData.getArticleSeqid(),
									articleData.getArticleNumber(),
									"approve",
									articleData.getReviewerComments(),
									articleData.getAdditionalReviewerComments(),
									metaDataField,
									articleData.getArticleOwnerSSO(),
									articleData.getReviewerid(),
									articleData.getAdditionalReviewerId());
							/*
							 * auditStatusUpdationOnArticleIssue
							 */
							// Log.info("Called when state is Issued~~~~~~~~~~~~~~~~~~~~~~~~");
							auditStatusUpdationOnArticleIssue(
									articleData.getFolderSeqId(),
									articleData.getArticleSeqid(),
									DRBUtils.setPersonnelFullName(
											userObj.getUserLastName(),
											userObj.getUserFirstName()));
						}

					} else {
						sendArticleTaskMail(articleData,
								articleData.getArticleTitle(),
								articleData.getArticleSeqid(),
								articleData.getArticleNumber(), "rejected",
								articleData.getReviewerComments(),
								articleData.getAdditionalReviewerComments(),
								metaDataField,
								articleData.getArticleOwnerSSO(),
								articleData.getReviewerid(),
								articleData.getAdditionalReviewerId());
						// Code Review Change on 16DEC10 By Naresh
					}

				}
				// Start: Added for asking user to submit the comment to
				// ArticleOwner
				else if (!articleEdited) {

					if (verificationPending) {
						getSimpleJdbcTemplate()
								.update(DRBQueryConstants.UPDATE_REVIEW_STATE,
										ArticleConstants.APPROVED_WITH_VERIFICATION_PENDING,
										articleData.getArticleSeqid());
					} else {

						getSimpleJdbcTemplate()
								.update(DRBQueryConstants.ARTICLE_REVIEW_STATE_TRANCE_TO_ISSUE,
										"", "", ArticleConstants.CHAR_N,
										ArticleConstants.EDRB_ACTON_FLAG03,
										ArticleConstants.APPROVED,
										articleData.getArticleSeqid());

						articleData
								.setChangedAttributeName(ArticleConstants.EDRB_ACTON_FLAG03);
						articleData.setOldValue(articleData.getArticleState());
						articleData
								.setNewValue(ArticleConstants.EDRB_ACTON_FLAG03);
						captureArticlesHistory(articleData);

						List<String> checkoutArticle = getSimpleJdbcTemplate()
								.query(DRBQueryConstants.CHECKOUT_ARTICLE,
										new ParameterizedRowMapper<String>() {
											public String mapRow(ResultSet rs,
													int arg1)
													throws SQLException {
												return rs
														.getString("STATE_NAME")
														+ "~"
														+ rs.getString("ARTICLE_SEQ_ID");
											}
										}, articleData.getArticleSeqid());
						UserData ownerUserObj = (UserData) (DRBUtils
								.getServletSession(false)
								.getAttribute(ArticleConstants.SESSION_USER_DATA));
						String userName = ownerUserObj.getUserLastName()
								+ ArticleConstants.EDRB_SEPARATOR
								+ ownerUserObj.getUserFirstName();
						int updatation = getSimpleJdbcTemplate()
								.update(DRBQueryConstants.ARTICLE_REVISE_ARTICLE_PROMOTE_ARTICLE_QUERY,
										new Object[] { userName,
												ArticleConstants.EDRB_NO_FLAG,
												articleData.getArticleSeqid() });
						if (updatation == 1) {

							if (checkoutArticle != null
									&& checkoutArticle.size() > 0) {
								articleData
										.setChangedAttributeName(ArticleConstants.EDRB_ACTON_FLAG03);
								articleData.setOldValue(checkoutArticle.get(0)
										.split(ArticleConstants.TILDA)[0]);
								articleData
										.setNewValue(ArticleConstants.EDRB_ACTON_FLAG03);
								articleData
										.setArticleSeqid(checkoutArticle.get(0)
												.split(ArticleConstants.TILDA)[1]);
								captureArticlesHistory(articleData);
							}

						}

						sendArticleTaskMail(articleData,
								articleData.getArticleTitle(),
								articleData.getArticleSeqid(),
								articleData.getArticleNumber(),
								ArticleConstants.STATE_ISSUEDWITHEDITS,
								primaryRevCommentsTotal,
								additionalRevCommentsTotal, metaDataField,
								articleData.getArticleOwnerSSO(),
								articleData.getReviewerid(),
								articleData.getAdditionalReviewerId());

						auditStatusUpdationOnArticleIssue(
								articleData.getFolderSeqId(),
								articleData.getArticleSeqid(),
								DRBUtils.setPersonnelFullName(
										userObj.getUserLastName(),
										userObj.getUserFirstName()));
					}

				}
				// End: Added for asking user to submit the comment to
				// ArticleOwner
				else {
					// Log.info("Task to owner");
					int seqID = getSimpleJdbcTemplate()
							.queryForInt(
									"SELECT CAWC.DRB_REQUEST_EVENT_SEQ.nextval FROM DUAL");
					String DRBE_REQUEST_EVENT = DRBQueryConstants.OWNER_APPROVAL_REQ;
					/********* insert a task for owner **************/
					getSimpleJdbcTemplate().update(
							DRBE_REQUEST_EVENT,
							seqID,
							articleData.getArticleSeqid(),
							userObj.getUserSSOID(),
							articleData.getArticleOwnerSSO(),
							"Title#:" + articleData.getArticleTitle()
									+ " has been edited by Reviewer(s)",
							ReviewName, ReviewName,
							articleData.getExportControlTagNumber(),
							ArticleConstants.CHAR_Y);
					sendArticleTaskMail(articleData,
							articleData.getArticleTitle(),
							articleData.getArticleSeqid(),
							articleData.getArticleNumber(), "edited",
							articleData.getReviewerComments(),
							articleData.getAdditionalReviewerComments(),
							metaDataField, articleData.getArticleOwnerSSO(),
							articleData.getReviewerid(),
							articleData.getAdditionalReviewerId());
					// Code Review Change on 16DEC10 By Naresh
				}
			}
		} else {
			// Log.info("Owner Approve");
			String userid = userObj.getUserSSOID();
			/********* owner login so it is ok **************/

			getSimpleJdbcTemplate().update(DRBQueryConstants.OWNER_APPROVAL,
					"", "", ArticleConstants.APPROVED, ArticleConstants.CHAR_Y,
					ArticleConstants.STATE_ISSUED, ArticleConstants.APPROVED,
					ReviewName, articleData.getArticleSeqid());
			// Start: Added by Pradeep on 28/01/2011 for Defect Id: 1455
			/* LOG.info("ArticleSeqId ......."+articleData.getArticleSeqid()); */
			String preRevisionSeqId = getArticleMetadataTitle(articleData
					.getArticleSeqid());
			/* LOG.info("PrevRevisionSeqId ............... :"+preRevisionSeqId); */
			/*********
			 * Start: Updated by Pradeep on 31/01/2011 for History Log in Revise
			 **************/
			if (articleData.getArticleTitle() != null
					&& _titleOld != null
					&& (!(articleData.getArticleTitle()
							.equalsIgnoreCase(_titleOld)))) {
				if (preRevisionSeqId != null) {
					/* LOG.info("-Approve Article Service Layer 4 -"); */
					String preTitle = _titleOld;
					if (newTitleValue != null
							&& preTitle != null
							&& !((newTitleValue.trim())
									.equalsIgnoreCase(preTitle.trim()))) {
						/* LOG.info("-Approve Article Service Layer 5-"); */
						getHistoryListChangeArticle(
								articleData.getArticleSeqid(),
								userObj.getUserSSOID());
					}
				}
			}
			/********* End: Updated by Pradeep on 31/01/2011 for History Log in Revise **************/
			// End: Added by Pradeep on 28/01/2011 for Defect Id: 1455
			String result = getDispositions(articleData.getArticleSeqid());
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_REVIEWER_REQ_EVENT_TAB,
					ArticleConstants.APPROVED, ArticleConstants.CHAR_N,
					ReviewName, articleData.getArticleSeqid(), userid,
					"Article Approval");
			if (reviewComments
					.equalsIgnoreCase(ArticleConstants.ISSUED_AFTER_ATTACHMENT_VERIFICATION)) {
				articleData
						.setChangedAttributeName("Issued After Attachment Verification");
				articleData.setOldValue("In Review");
				articleData.setNewValue("Issued");
			} else {
				articleData.setChangedAttributeName("Accepted Changes");
				articleData.setOldValue("");
				articleData.setNewValue("");

			}
			captureArticlesHistory(articleData);

			// capture history
			/**** lasted updated **********/
			List<String> checkoutArticle = getSimpleJdbcTemplate()
					.query("SELECT STATE_NAME,ARTICLE_SEQ_ID FROM CAWC.DRB_ARTICLE A,CAWC.DRB_WF_STATE S WHERE "
							+ "ARTICLE_SEQ_ID=(SELECT REVISION_ARTICLE_SEQ_ID FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID=?) "
							+ "AND A.ARTICLE_WF_STATE_SEQ_ID=S.WF_STATE_SEQ_ID",
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int arg1)
										throws SQLException {
									return rs.getString("STATE_NAME") + "~"
											+ rs.getString("ARTICLE_SEQ_ID");
								}
							}, articleData.getArticleSeqid());
			UserData ownerUserObj = (UserData) (DRBUtils
					.getServletSession(false)
					.getAttribute(ArticleConstants.SESSION_USER_DATA));
			String userName = ownerUserObj.getUserLastName()
					+ ArticleConstants.EDRB_SEPARATOR
					+ ownerUserObj.getUserFirstName();
			int updatation = getSimpleJdbcTemplate()
					.update(DRBQueryConstants.ARTICLE_REVISE_ARTICLE_PROMOTE_ARTICLE_QUERY,
							new Object[] { userName, "N",
									articleData.getArticleSeqid() });
			if (updatation == 1) {

				if (checkoutArticle != null && checkoutArticle.size() > 0) {
					articleData.setChangedAttributeName(result.split("~")[0]);
					articleData
							.setOldValue(checkoutArticle.get(0).split("~")[0]);
					articleData.setNewValue(result.split("~")[0]);
					articleData.setArticleSeqid(checkoutArticle.get(0).split(
							"~")[1]);
					captureArticlesHistory(articleData);

				}

			}

			sendArticleTaskMail(articleData, articleData.getArticleTitle(),
					articleData.getArticleSeqid(),
					articleData.getArticleNumber(), "owner",
					articleData.getReviewerComments(),
					articleData.getAdditionalReviewerComments(), metaDataField,
					articleData.getArticleOwnerSSO(),
					articleData.getReviewerid(),
					articleData.getAdditionalReviewerId());// Code Review Change
															// on 16DEC10 By
															// Naresh
			/*
			 * auditStatusUpdationOnArticleIssue
			 */
			// Log.info("Called In Else @@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			auditStatusUpdationOnArticleIssue(articleData.getFolderSeqId(),
					articleData.getArticleSeqid(),
					DRBUtils.setPersonnelFullName(userObj.getUserLastName(),
							userObj.getUserFirstName()));
		}

		/*
		 * An Email has to send to Owner Regarding Process
		 */

		// Log.info("--->END Of Approve Article Method<---");
		return "Success";
	}

	/**
	 * @param toUpdate
	 * @param articleData
	 * @return String
	 */
	public String updationOnReviewStatus(String toUpdate,
			ArticleData articleData) {
		String toReturn = "N";
		// Log.info("toUpdate         "+ toUpdate);
		if ("N".equalsIgnoreCase(toUpdate)) {
			toReturn = "N";
		} else {
			UserData userObj = (UserData) (DRBUtils.getServletSession(false)
					.getAttribute(ArticleConstants.SESSION_USER_DATA));
			if (userObj.getUserSSOID().equalsIgnoreCase(
					articleData.getAdditionalReviewerId())// Code Review Change
															// on 16DEC10 By
															// Naresh
					&& ("A".equalsIgnoreCase(toUpdate) || "Y"
							.equalsIgnoreCase(toUpdate))) {
				toReturn = "Y";
			}
			if (userObj.getUserSSOID().equalsIgnoreCase(
					articleData.getReviewerid())
					&& ("P".equalsIgnoreCase(toUpdate) || "Y"
							.equalsIgnoreCase(toUpdate))) {
				toReturn = "Y";
			}
		}
		// Log.info(">>>>>>>>>>>>>>>" + toReturn);
		return toReturn;
	}

	/**
	 * @param articleSeqId
	 * @return String
	 */
	public String getDispositions(String articleSeqId) {
		/*
		 * SETTING ARTICLE_WF_STATE_SEQ_ID If both
		 * REVIEW_DISPOSTION,ADDITIONAL_REVIEW_DISPOSTION are Approved then
		 * Article will be in Issues State
		 */
		StringBuffer ARTICLE_STATUS = new StringBuffer();
		ARTICLE_STATUS.append(DRBQueryConstants.REVIEWERS_DISPOSITOINS);
		List<String> articleStatus = getSimpleJdbcTemplate().query(
				ARTICLE_STATUS.toString(),
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						String reviewStatus = "";
						String additionalReviewStatus = "";
						String ownerStatus = "";
						String mainStatus = "";
						if (rs.getString("REVIEWER_USER_ID") != null
								&& !"".equals(rs.getString("REVIEWER_USER_ID"))) {
							if (rs.getString("REVIEW_DISPOSITION") != null
									&& rs.getString("REVIEW_DISPOSITION")
											.equalsIgnoreCase("APPROVED")) {
								reviewStatus = "A";
							} else if (rs.getString("REVIEW_DISPOSITION") != null
									&& rs.getString("REVIEW_DISPOSITION")
											.equalsIgnoreCase("REJECTED")) {
								reviewStatus = "R";
								mainStatus = null;
							} else {
								reviewStatus = null;
							}
						}
						// Log.info("Reviwer>>>>>>>>>>>>>>>>>Status" +
						// reviewStatus);
						if (rs.getString("ADDITIONAL_REVIEWER_USER_ID") != null
								&& !"".equals(rs
										.getString("ADDITIONAL_REVIEWER_USER_ID"))) {
							if (rs.getString("ADDITIONAL_REVIEW_DISPOSITION") != null
									&& rs.getString(
											"ADDITIONAL_REVIEW_DISPOSITION")
											.equalsIgnoreCase("APPROVED")) {
								additionalReviewStatus = "A";
							} else if (rs
									.getString("ADDITIONAL_REVIEW_DISPOSITION") != null
									&& rs.getString(
											"ADDITIONAL_REVIEW_DISPOSITION")
											.equalsIgnoreCase("REJECTED")) {
								additionalReviewStatus = "R";
								mainStatus = null;
							} else {
								additionalReviewStatus = null;
							}
						} else {
							additionalReviewStatus = "A";
						}
						// Log.info("ADD Reviwer>>>>>>>>>>>>>>>>>Status" +
						// additionalReviewStatus);

						if (rs.getString("OWNER_USER_ID") != null
								&& !"".equals(rs.getString("OWNER_USER_ID"))) {
							if (rs.getString("OWNER_REVIEW_DISPOSITION") != null
									&& rs.getString("OWNER_REVIEW_DISPOSITION")
											.equalsIgnoreCase("APPROVED")) {
								ownerStatus = "A";
							} else if ("REJECTED".equalsIgnoreCase(rs
									.getString("OWNER_REVIEW_DISPOSITION"))) {
								ownerStatus = "R";
							} else {
								ownerStatus = null;
							}
						}
						// Log.info("Owner Dis>>>>>>>>>>>>>>>>>Status" +
						// ownerStatus);

						if (mainStatus != null) {
							if (("Y".equalsIgnoreCase(rs
									.getString("REVIEWER_METADATA_EDIT_IND"))
									|| "P".equalsIgnoreCase(rs
											.getString("REVIEWER_METADATA_EDIT_IND")) || "A".equalsIgnoreCase(rs
									.getString("REVIEWER_METADATA_EDIT_IND")))) {

								mainStatus = "Y";
							} else {
								mainStatus = "N";
							}
						}

						if ("R".equals(reviewStatus)
								|| "R".equals(additionalReviewStatus)) {
							reviewStatus = "R";
							additionalReviewStatus = "R";
						}
						String totalStatus = reviewStatus + "~"
								+ additionalReviewStatus + "~" + mainStatus
								+ "~" + ownerStatus;
						// Log.info("Total Status>>>>>>>>>>>>>>>>>>>>>>>>" +
						// totalStatus);

						return totalStatus;
					}
				}, articleSeqId);

		String ArticleStateTrans = "SELECT STATE_NAME,WF_STATE_SEQ_ID FROM CAWC.DRB_WF_STATE WHERE "
				+ "WF_STATE_SEQ_ID=(SELECT TO_WF_STATE_SEQ_ID FROM CAWC.DRB_WF_STATE_TRANS WHERE "
				+ "WF_STATE_TRANS_SEQ_ID=(SELECT WF_STATE_TRANS_SEQ_ID FROM CAWC.DRB_ARTICLE_STATETRANS "
				+ "WHERE ARTICLE_REVIEW_REQUIRED_IND='Y' AND ";
		String totalStatus = articleStatus.get(0);
		String returnResult = "";
		List<String> stateTrans = null;// new ArrayList<String>();
		String[] preQuey = { "PRIMARY_REVIEW_DISPOSITION",
				"ADDITIONAL_REVIEW_DISPOSITION", "ARTICLE_METADATA_EDIT_IND",
				"ARTICLE_OWNER_DISPOSITION_IND" };
		String[] dynQuery = totalStatus.split("~");
		StringBuffer queryString = new StringBuffer();
		for (int count = 0; count < 4; count++) {
			if (dynQuery[count].equalsIgnoreCase("null")) {
				queryString.append(preQuey[count]).append(" IS NULL AND ");
			} else {
				queryString.append(preQuey[count]).append("= '")
						.append(dynQuery[count]).append("' AND ");
			}
		}
		ArticleStateTrans += queryString.substring(0, queryString.length() - 4)
				+ " ))";
		// Log.info("<<<<<<<<<<<<<<:::::::::::::::::" + ArticleStateTrans);
		if (totalStatus.split("~").length == 4) {
			if (!totalStatus.split("~")[0].equalsIgnoreCase("null")
					&& !totalStatus.split("~")[1].equalsIgnoreCase("null")) {
				stateTrans = getSimpleJdbcTemplate().query(ArticleStateTrans,
						new ParameterizedRowMapper<String>() {
							public String mapRow(ResultSet rs, int arg1)
									throws SQLException {
								return rs.getString("STATE_NAME") + "~"
										+ rs.getString("WF_STATE_SEQ_ID");
							}
						});
				if (stateTrans != null && stateTrans.size() > 0) {
					returnResult = stateTrans.get(0);
				} else {
					returnResult = "In Review";
				}
			} else {
				returnResult = "NO";
			}
		}
		// Log.info("return Resut>>>>>>>>>>>>>>>>" + returnResult);
		ARTICLE_STATUS = null;// heap issue
		return returnResult;
	}

	/**
	 * @param articleNo
	 * @param revComments
	 * @param taskSeqId
	 * @param status
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String rejectArticle(ArticleData articleData, String revComments,
			String status) throws DRBCommonException {
		// Log.info("Article Number " + articleData.getArticleSeqid() +
		// "Rejected Commnents  " + comments);
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String userids = userObj.getUserSSOID();
		String ReviewName = userObj.getUserLastName() + ", "
				+ userObj.getUserFirstName();
		String getReviewComments = null;
		List<String> reviewerComments = null;
		String prevReviewComments = null;

		String reviewComments = revComments;

		if (status.equals("Review Article")) {
			String REJECT_ARTICLE_BY_SEQ_ID = null;
			/*
			 * have to know role of reviewer[NOTE:true--->default Review] and
			 * have to set an e-mail to owner.
			 */
			String userid = "";
			if (userObj.getUserSSOID().equalsIgnoreCase(
					articleData.getReviewerid())) {
				REJECT_ARTICLE_BY_SEQ_ID = DRBQueryConstants.PRIMARY_REVIEWER_REJECT;
				getReviewComments = DRBQueryConstants.GET_REVIEWER_COMMENTS;
				userid = articleData.getReviewerid();
				articleData.setReviewerComments(reviewComments);
				articleData.setReviewerDisposition(ArticleConstants.REJECTED);
			} else {
				REJECT_ARTICLE_BY_SEQ_ID = DRBQueryConstants.ADDITIONAL_REVIEWER_REJECT;
				getReviewComments = DRBQueryConstants.GET_ADDTIONAL_REVIEWER_COMMENTS;
				userid = articleData.getAdditionalReviewerId();
				articleData.setAdditionalReviewerComments(reviewComments);
				articleData
						.setAdditionalReviewerDisposition(ArticleConstants.REJECTED);
			}
			reviewerComments = getSimpleJdbcTemplate().query(getReviewComments,
					new ParameterizedRowMapper<String>() {
						public String mapRow(ResultSet rs, int arg1)
								throws SQLException {
							String edited = rs.getString("REVIEW_COMMENTS");
							return edited;
						}
					}, articleData.getArticleSeqid());
			if (reviewerComments != null && reviewerComments.size() > 0) {
				prevReviewComments = reviewerComments.get(0);
			}
			if (prevReviewComments != null && !prevReviewComments.equals("")) {
				reviewComments = prevReviewComments
						+ "\n\n --------------------\n\n" + reviewComments;
			}
			getSimpleJdbcTemplate().update(REJECT_ARTICLE_BY_SEQ_ID, 1,
					reviewComments, ReviewName, ArticleConstants.REJECTED,
					articleData.getArticleSeqid());
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_REVIEWER_REQ_EVENT_TAB,
					ArticleConstants.REJECTED, ArticleConstants.CHAR_N,
					ReviewName, articleData.getArticleSeqid(), userid,
					"Review Article");
			if (!DRBUtils.isEmpty(articleData.getReviewerDisposition())
					&& articleData.getReviewerDisposition().equalsIgnoreCase(
							ArticleConstants.REJECTED)) {
				articleData
						.setChangedAttributeName("Primary reviewer disposition");
			}
			if (!DRBUtils.isEmpty(articleData
					.getAdditionalReviewerDisposition())
					&& articleData.getAdditionalReviewerDisposition()
							.equalsIgnoreCase(ArticleConstants.REJECTED)) {
				articleData
						.setChangedAttributeName("Secondary reviewer disposition");
			}

			articleData.setOldValue("");
			articleData.setNewValue(ArticleConstants.REJECTED);

			// End of History capture
			String result = getDispositions(articleData.getArticleSeqid());
			if (!"NO".equalsIgnoreCase(result)) {
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.ARTICLE_REVIEW_STATE_TRANCE_TO_DRAFT,
						result.split("~")[0], ArticleConstants.REJECTED,
						articleData.getArticleSeqid());
				// For Rejected to Draft
				ArticleData articleObj = new ArticleData();
				articleObj.setChangedAttributeName("Rejected to Draft");
				articleObj.setOldValue("");
				articleObj.setNewValue("");
				articleObj.setArticleSeqid(articleData.getArticleSeqid());
				captureArticlesHistory(articleObj);
				// capturehistory

				// Remove the other reviewers Task if available
				String theReviewerTaskUpateQuery = "UPDATE CAWC.DRB_REQUEST_EVENT set ACTIVE_IND='N' where"
						+ " RECIPIENT_USER_ID in (?,?) and request_type_id=(select request_type_id from "
						+ "CAWC.DRB_REQUEST_TYPE where request_type_name='Review Article') and APP_DATA_PK_ID=? and ACTIVE_IND='Y'";
				getSimpleJdbcTemplate().update(theReviewerTaskUpateQuery,
						articleData.getReviewerid(),
						articleData.getAdditionalReviewerId(),
						articleData.getArticleSeqid());

				sendArticleTaskMail(articleData, articleData.getArticleTitle(),
						articleData.getArticleSeqid(),
						articleData.getArticleNumber(), "rejected",
						articleData.getReviewerComments(),
						articleData.getAdditionalReviewerComments(), "",
						articleData.getArticleOwnerSSO(),
						articleData.getReviewerid(),
						articleData.getAdditionalReviewerId());
				// Code Review Change on 16DEC10 By Naresh
			}

		} else {
			// Log.info("Owner Reject");
			StringBuffer OWNER_ARTICLE_BY_SEQ_ID = new StringBuffer();
			OWNER_ARTICLE_BY_SEQ_ID.append(DRBQueryConstants.OWNER_REJECT);

			getSimpleJdbcTemplate().update(OWNER_ARTICLE_BY_SEQ_ID.toString(),
					ArticleConstants.REJECTED, 1, ReviewName,
					ArticleConstants.DRAFT, ArticleConstants.REJECTED,
					articleData.getArticleSeqid());
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_REVIEWER_REQ_EVENT_TAB,
					ArticleConstants.REJECTED, ArticleConstants.CHAR_N,
					ReviewName, articleData.getArticleSeqid(), userids,
					"Article Approval");
			articleData.setChangedAttributeName("Rejected changes");
			articleData.setOldValue("");
			articleData.setNewValue("");
			captureArticlesHistory(articleData);
			// capture history
			sendArticleTaskMail(articleData, articleData.getArticleTitle(),
					articleData.getArticleSeqid(),
					articleData.getArticleNumber(), "ownerreject",
					articleData.getReviewerComments(),
					articleData.getAdditionalReviewerComments(), "",
					articleData.getArticleOwnerSSO(),
					articleData.getReviewerid(),
					articleData.getAdditionalReviewerId());// Code Review Change
															// on 16DEC10 By
															// Naresh
			OWNER_ARTICLE_BY_SEQ_ID = null; // heap issue

		}

		/*
		 * An Email has to send to Owner Regarding Process
		 */

		// Log.info("--->END Of Reject Article Method<---");
		return "Success";
	}

	/**
	 * This method is used to check whether the article meta data is edited or
	 * not
	 * 
	 * @param articleNo
	 * @return String
	 * @throws DRBCommonException
	 */
	public String isArticleMetaDataEdited(String articleNo) {
		/*
		 * check whether flag is On (or) Off [Default NO]
		 */

		String IS_ARTICLE_EDITED = DRBQueryConstants.IS_ARTICLE_EDITED;

		List<String> isEdited = getSimpleJdbcTemplate().query(
				IS_ARTICLE_EDITED, new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						String edited = rs
								.getString("REVIEWER_METADATA_EDIT_IND");
						return edited;
					}
				}, articleNo);

		String editedStatus = isEdited.get(0);

		return editedStatus;
	}

	/**
	 * @param articleSeqId
	 * @return
	 */
	public FolderMetaData checkArticleSource(String articleSeqId)
			throws DRBCommonException {
		FolderMetaData folderData = null;// new FolderMetaData();
		folderData = getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.GET_ARTICLE_FOLDER_MOVE_DETAILS,
				new ParameterizedRowMapper<FolderMetaData>() {
					public FolderMetaData mapRow(ResultSet rs, int arg1)
							throws SQLException {
						FolderMetaData folData = new FolderMetaData();
						folData.setFolderId(rs.getString("FOLDER_SEQ_ID"));
						folData.setLockInd(rs.getString("LOCK_IND"));
						folData.setFolNumStyle(rs
								.getString("NUMBERING_STYLE_NAME"));
						folData.setLibNumStyle(rs
								.getString("LIBRARY_NUMBERING_STYLE"));
						return folData;
					}
				}, articleSeqId);
		return folderData;
	}

	/**
	 * This method is used to distinguish the Primary / Additional Reviewer
	 * 
	 * @param userRole
	 * @param articleNo
	 * @return Integer
	 * @throws DRBCommonException
	 */
	public int getArticleReviewer(String userRole, String articleNo)
			throws DRBCommonException {
		// Log.info("Inside into the DAO ==>>> getArticleReviewer()");
		int reviewerVal = 0;
		return reviewerVal;
	}

	/**
	 * @param mapParam
	 * @param userObj
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String updateArticleOwnerOrReviewer(String ectValue,
			Map<String, String> mapParam, UserData userObj)
			throws DRBCommonException {
		// Log.info("The LOGED USER DETAILS ARE IN DAO===>>" +
		// userObj.getUserFirstName() + " " + userObj.getUserSSOID() + " "
		// + userObj.getUserEMailID());
		String userName = userObj.getUserLastName()
				+ ArticleConstants.SEPARATOR + userObj.getUserFirstName();
		String updateQuery = null;
		String retVal = null;
		String emailsIds = null;
		int val = 0;
		Map messageMap = null;// new HashMap();
		String articleNumber = (String) mapParam.get("articleNumber");
		if ((String) mapParam.get("userRole") != null
				&& ArticleConstants.EDRB_ROLE_01
						.equalsIgnoreCase((String) mapParam.get("userRole"))) {

			String articleCode = "notused";
			messageMap = _changeArticleOwnerPrc.executeChangeArticleOwner(
					Integer.parseInt((String) mapParam.get("articleSeqId")),
					(String) userObj.getUserSSOID(), articleCode,
					(String) mapParam.get("userID"), userName,
					ArticleConstants.CHANGE_OWNER_REQUEST_DISCRIPTION);
			if (messageMap != null)
				emailsIds = (String) messageMap.get("P_OUT_PROC_EXEC_MSG");
			if (emailsIds != null && emailsIds.contains(ArticleConstants.TILDA)) {
				val = 1;
			} else {
				val = 0;
			}
		} else if ((String) mapParam.get("userRole") != null
				&& ArticleConstants.EDRB_ROLE_03
						.equalsIgnoreCase((String) mapParam.get("userRole"))) {

			if ((String) mapParam.get("action") != null
					&& ((String) mapParam.get("action"))
							.equalsIgnoreCase(ArticleConstants.ASSIGN_ACTION)) {
				updateQuery = DRBQueryConstants.ASSIGN_ARTICLE_REVIEWER;
				val = getSimpleJdbcTemplate().update(
						updateQuery,
						new Object[] {
								(String) mapParam.get("userID"),
								userName,
								Integer.parseInt((String) mapParam
										.get("articleSeqId")) });
			} else if ((String) mapParam.get("action") != null
					&& ((String) mapParam.get("action"))
							.equalsIgnoreCase(ArticleConstants.REASSIGN_ACTION)) {
				updateQuery = DRBQueryConstants.REASSIGN_ARTICLE_REVIEWER;
				val = getSimpleJdbcTemplate().update(
						updateQuery,
						new Object[] {
								(String) mapParam.get("userID"),
								userName,
								Integer.parseInt((String) mapParam
										.get("articleSeqId")) });
				// Log.info("REASSSING REVIEWER METHOD****after***********" +
				// Integer.parseInt((String) mapParam.get("articleSeqId")));
				String taskDesc = "Article #: "
						+ (String) mapParam.get("articleNumber")
						+ " reassigned for review as Primary Reviewer";
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.REASSIGN_REVIEWER_REQUEST_EVENT,
						new Object[] {
								userObj.getUserSSOID(),
								mapParam.get("userID"),
								taskDesc,
								userName,
								Integer.parseInt((String) mapParam
										.get("articleSeqId")),
								userObj.getUserSSOID() });
			}

		} else if ((String) mapParam.get("userRole") != null
				&& ArticleConstants.EDRB_ROLE_04
						.equalsIgnoreCase((String) mapParam.get("userRole"))) {
			updateQuery = DRBQueryConstants.ADD_ADDITIONAL_ARTICLE_REVIEWER;
			val = getSimpleJdbcTemplate().update(
					updateQuery,
					new Object[] {
							(String) mapParam.get("userID"),
							userName,
							Integer.parseInt((String) mapParam
									.get("articleSeqId")) });

			String taskDescription = "Article #: "
					+ (String) mapParam.get("articleNumber")
					+ " assigned for review as Additional Reviewer";
			// Log.info("The description which is going to be print ::::" +
			// taskDescription);
			if ((String) mapParam.get("articleState") != null
					&& (!"Draft".equalsIgnoreCase((String) mapParam
							.get("articleState")))) {
				if ((String) mapParam.get("task") != null
						&& "update".equalsIgnoreCase((String) mapParam
								.get("task"))) {
					// Log.info("The inside the DAO Layer " + (String)
					// mapParam.get("task"));
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.REASSIGN_REVIEWER_REQUEST_EVENT,
							new Object[] {
									userObj.getUserSSOID(),
									mapParam.get("userID"),
									taskDescription,
									userName,
									Integer.parseInt((String) mapParam
											.get("articleSeqId")),
									mapParam.get("additionalReviewerId") });
				} else {
					getSimpleJdbcTemplate()
							.update(DRBQueryConstants.ADD_ADDITIONAL_REVIEWER_REQUEST_EVENT,
									new Object[] {
											mapParam.get("articleSeqId"),
											userObj.getUserSSOID(),
											mapParam.get("userID"),
											taskDescription,
											mapParam.get("libraryId"),
											userName, userName });
				}
			}

		} else if ((String) mapParam.get("userRole") != null
				&& "secowner".equalsIgnoreCase((String) mapParam
						.get("userRole"))) {
			updateQuery = DRBQueryConstants.ADD_SEC_OWNER;
			val = getSimpleJdbcTemplate().update(
					updateQuery,
					new Object[] {
							(String) mapParam.get("userID"),
							userName,
							Integer.parseInt((String) mapParam
									.get("articleSeqId")) });
		}

		if (val != 0 && ((String) mapParam.get("userRole") != null)) {
			if ((String) mapParam.get("action") != null
					&& ((String) mapParam.get("action"))
							.equalsIgnoreCase(ArticleConstants.ASSIGN_ACTION)) {
				retVal = ArticleConstants.SAVE_SUCCESS;
			} else {

				sendArticleUserMail(ectValue, emailsIds,
						(String) articleNumber,
						(String) mapParam.get("userRole"),
						(String) mapParam.get("action"),
						(String) mapParam.get("articleState"),
						(String) mapParam.get("userID"),
						(String) mapParam.get("ownerSSOid"),
						(String) mapParam.get("selectedUserName"), userName,
						(String) mapParam.get("articleSeqId"));
				retVal = ArticleConstants.SAVE_SUCCESS;
			}
		} else {
			retVal = ArticleConstants.ERROR;
		}
		return retVal;
	}

	/**
	 * @param libNo
	 * @param folNo
	 * @return ArticleData
	 * @throws DRBCommonException
	 */
	public ArticleData getParentData(String libNo, String folNo)
			throws DRBCommonException {

		/*
		 * ViewArticleProc viewArticleProc = null; ApplicationContext context =
		 * null;
		 * 
		 * ServletContext _serContext = (ServletContext)
		 * FacesContext.getCurrentInstance().getExternalContext().getContext();
		 * context =
		 * WebApplicationContextUtils.getWebApplicationContext(_serContext);
		 * viewArticleProc = (ViewArticleProc)
		 * context.getBean("viewArticleProc");
		 */

		ArticleData articleData;
		articleData = new ArticleData();
		// Log.info("getParentData :: library NO ==>>> " + libNo);
		// Log.info("getParentData :: folder NO ==>>> " + folNo);
		articleData.setArticleSeqid("0");
		articleData.setFolderSeqId(folNo);
		Map out = null;// new HashMap();
		out = viewArticleProc.executeViewArticle(0, "", "CREATE",
				Integer.parseInt(folNo), articleData, "false");
		if (out != null)
			articleData = (ArticleData) out.get("articleData");
		// //Log.info("getParentData :: end of proc " + out.size());

		articleData.setIpTag(ArticleConstants.CHAR_U);
		articleData.setArticleState(ArticleConstants.DRAFT);
		articleData.setArticleIsPaper(ArticleConstants.CHAR_N); // Overiding the
																// paper value
																// to get

		/*
		 * Start: Added for Use Case 03: Add Properties field for engine/product
		 * like it is in DR
		 */
		articleData = getPropertyList(articleData);
		/*
		 * End: Added for Use Case 03: Add Properties field for engine/product
		 * like it is in DR
		 */

		return articleData;
	}

	/**
	 * @param seqId
	 * @return List<String>
	 */
	public List<String> saveModifiedData(String seqId) {
		List<String> modifiedList = new ArrayList<String>();
		String saveModifiedDataQuery = "SELECT REVIEWER_METADATA_EDIT FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID = ?";
		List<String> listSingleRow = getSimpleJdbcTemplate().query(
				saveModifiedDataQuery, new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("REVIEWER_METADATA_EDIT");
					}
				}, seqId);
		String[] Arraydata = null;
		if (listSingleRow != null && listSingleRow.get(0) != null) {
			Arraydata = listSingleRow.get(0).split(",");
		}

		if (Arraydata != null) {
			for (String temp : Arraydata) {
				modifiedList.add(temp);
			}
		}
		return modifiedList;
	}

	/**
	 * @param parameterArticleTO
	 * @return ArticleData
	 * @throws DRBCommonException
	 */
	@Transactional
	public ArticleData saveArticle(ArticleData argArticleData,
			List<String> toSaveUpdate) throws DRBCommonException {
		/* ViewArticleProc viewArticleProc = null; */
		/* ApplicationContext context = null; */
		String prevArtSeqId = null;
		/*
		 * ServletContext _serContext = (ServletContext)
		 * FacesContext.getCurrentInstance().getExternalContext().getContext();
		 * context =
		 * WebApplicationContextUtils.getWebApplicationContext(_serContext);
		 * viewArticleProc = (ViewArticleProc)
		 * context.getBean("viewArticleProc");
		 */
		List<String> emailIds = new ArrayList<String>();
		ArticleData articleData = argArticleData;
		String newdataClassName = ArticleConstants.EMPTY;
		String oldDataClassName = ArticleConstants.EMPTY;
		LOG.info("Button name in savemethod" + articleData.getButtonName());
		if ("reviewArticleMetaDataSave".equalsIgnoreCase(articleData
				.getButtonName())) {
			StringBuffer dataToSave = new StringBuffer("");
			List<String> savedTempData = saveModifiedData(articleData
					.getArticleSeqid());
			List<String> tempDataToSave = null;
			if (toSaveUpdate != null) {
				tempDataToSave = toSaveUpdate;
			} else {
				tempDataToSave = new ArrayList<String>();
			}
			if (savedTempData != null && savedTempData.size() > 0) {
				for (String tempData : savedTempData) {
					if (null != toSaveUpdate && toSaveUpdate.contains(tempData)) {
						;
					} else {
						tempDataToSave.add(tempData);
					}
				}
			}
			for (String tempdata : tempDataToSave) {
				dataToSave.append(tempdata).append(",");
			}

			// Log.info("modified List>>>>>>>>>>>>>>>>>>>> to DataBase" +
			// dataToSave.toString());
			String toReturn = "";
			StringBuffer updatingDB = new StringBuffer();
			updatingDB.append(DRBQueryConstants.SAVE_ARTICLE_META_DATA);
			// Log.info("Static String " + updatingDB);
			UserData userObj = (UserData) (DRBUtils.getServletSession(false)
					.getAttribute(ArticleConstants.SESSION_USER_DATA));
			if (userObj != null) {

				String FLAG = "SELECT REVIEWER_METADATA_EDIT_IND FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID=?";
				List<String> flagCheck = getSimpleJdbcTemplate().query(FLAG,
						new ParameterizedRowMapper<String>() {
							public String mapRow(ResultSet rs, int arg1)
									throws SQLException {
								return rs
										.getString("REVIEWER_METADATA_EDIT_IND");
							}
						}, articleData.getArticleSeqid());
				String tempgeneralPool = "";
				String temparticleIsPaper = "";
				String tempipTag = ArticleConstants.EMPTY;
				if (articleData.getArticleIsPaper() != null
						&& "Yes".equalsIgnoreCase(articleData
								.getArticleIsPaper())) {
					temparticleIsPaper = "Y";
				} else {
					temparticleIsPaper = "N";
				}
				tempipTag = articleData.getIpTag();

				if (ArticleConstants.YES_STRING.equalsIgnoreCase(tempipTag)) {
					tempipTag = ArticleConstants.CHAR_Y;
				} else if (ArticleConstants.NO_STRING
						.equalsIgnoreCase(tempipTag)) {
					tempipTag = ArticleConstants.CHAR_N;
				} else if (ArticleConstants.UNK_STRING
						.equalsIgnoreCase(tempipTag)) {
					tempipTag = ArticleConstants.CHAR_U;
				}

				if ("Yes".equalsIgnoreCase(articleData.getGeneralpool())) {
					tempgeneralPool = "Y";
				} else if ("No".equalsIgnoreCase(articleData.getGeneralpool())) {
					tempgeneralPool = "N";
				}
				if (!DRBUtils.isNullOrEmpty(articleData
						.getGeDocumentClassNumber())) {
					newdataClassName = getSimpleJdbcTemplate().queryForObject(
							DRBQueryConstants.SELECT_DATA_CLASSIFICATION_NAME,
							docClassifcationMapper,
							new Object[] { articleData
									.getGeDocumentClassNumber() });
					oldDataClassName = getSimpleJdbcTemplate().queryForObject(
							DRBQueryConstants.DATA_CLASS_ARTICLE,
							docClassifcationMapper,
							new Object[] { articleData.getArticleSeqid() });
				}
				if (flagCheck != null && !flagCheck.equals("")
						&& flagCheck.size() > 0) {
					// Log.info(flagCheck.get(0));
					toReturn = flagCheck.get(0);
					if (flagCheck.get(0) == null
							|| flagCheck.get(0).equalsIgnoreCase("N")) {
						if (userObj.getUserSSOID().equalsIgnoreCase(
								articleData.getReviewerid())) {
							toReturn = "P";
						} else {
							toReturn = "A";
						}
					} else if (userObj.getUserSSOID().equalsIgnoreCase(
							articleData.getReviewerid())
							&& "P".equalsIgnoreCase(flagCheck.get(0))) {
						toReturn = "P";
					} else if (userObj.getUserSSOID().equalsIgnoreCase(
							articleData.getAdditionalReviewerId())
							&& "A".equalsIgnoreCase(flagCheck.get(0))) {
						toReturn = "A";
					} else {
						toReturn = "Y";
					}

					getSimpleJdbcTemplate().update(
							updatingDB.toString(),
							articleData.getArticleTitle(),
							articleData.getArticleSubject(),
							articleData.getKeyWords(),
							Integer.parseInt(articleData
									.getOwningBusinessSegment()),
							temparticleIsPaper,
							articleData.getArticleAbstract(),
							Integer.parseInt(articleData
									.getGeDocumentClassNumber()),
							articleData.getReferences(),
							articleData.getIpIssue(),
							tempipTag,
							articleData.getProblemOrPurpose(),
							articleData.getAssumptions(),
							articleData.getResults(),
							articleData.getConclusions(),
							articleData.getArticleAuthors(),
							articleData.getSecOwnerId(),
							articleData.getLegacyArticleNumber(),
							articleData.getReviewStatus(),
							toReturn,
							userObj.getUserSSOID(),
							tempgeneralPool,
							dataToSave.toString(),
							articleData.getDrNumber(),
							articleData.getAiNumber(),
							// Added for Use Case 03: Add Properties field for
							// engine/product like it is in DR
							articleData.getEngineModelId(),
							articleData.getReviewAreaId(),
							articleData.getDisciplineAreaId(),
							articleData.getArtErrortype(),
							articleData.getArticleSeqid());
					LOG.info("articleData.getArtErrortype(),---In dao" +articleData.getArtErrortype());
					LOG.info("articleData.getArticleSeqid());---In dao" +articleData.getArticleSeqid());
					// Avinash Code Ends.

					// Added for capturing GE Classification
					if (!oldDataClassName.equalsIgnoreCase(newdataClassName)) {
						articleData.setOldValue(oldDataClassName);
						articleData.setNewValue(newdataClassName);
						articleData
								.setChangedAttributeName("DocumentClassification");// Modified
																					// by
																					// Pradeep
						// on 05/02/2011 for Defect Id: 1495
						articleData.setArticleSeqId(Integer
								.parseInt(articleData.getArticleSeqid()));
						captureArticlesHistory(articleData);
						// //Log.info("the status" + histStatus);
					}
					// End of capturing GE Classification

				}

			}
			articleData.setReturnVariable(toReturn);
			return articleData;
		} else {
			// For Create Article
			// For save article
			if (("editArticleSave").equals(articleData.getButtonName())) {
				// for review article 21-01-09
				UserData ownerUserObj = (UserData) (DRBUtils
						.getServletSession(false)
						.getAttribute(ArticleConstants.SESSION_USER_DATA));

				String userName = ownerUserObj.getUserLastName()
						+ ArticleConstants.EDRB_SEPARATOR
						+ ownerUserObj.getUserFirstName();
				String query = DRBQueryConstants.ARTICLE_CREATE_ARTICLE_SAVE_ARTICLE_QUERY;

				// Added by 307009315 for updating new fields in ODA RR - 4 Nov
				// 2013

				String odaUpdateQuery = DRBQueryConstants.ODARR_RECORD_UPDATE_QUERY;

				// Added by 307009315 ends

				if (!DRBUtils.isNullOrEmpty(articleData
						.getGeDocumentClassNumber())) {
					newdataClassName = getSimpleJdbcTemplate().queryForObject(
							DRBQueryConstants.SELECT_DATA_CLASSIFICATION_NAME,
							docClassifcationMapper,
							new Object[] { articleData
									.getGeDocumentClassNumber() });
					oldDataClassName = getSimpleJdbcTemplate().queryForObject(
							DRBQueryConstants.DATA_CLASS_ARTICLE,
							docClassifcationMapper,
							new Object[] { articleData.getArticleSeqid() });
				}
				// LOG.info("Before Update Query================"+articleData.getGeneralpool()
				// );
				// Added By Naresh for GENERALPOOL ISSUE Start
				if (articleData.getGeneralpool() != null) {
					if ("Yes".equalsIgnoreCase(articleData.getGeneralpool())) {
						articleData.setGeneralpool("Y");
					} else if ("No".equalsIgnoreCase(articleData
							.getGeneralpool())) {
						articleData.setGeneralpool("N");
					}
				} else {
					articleData.setGeneralpool("");
				}
				// Added By Naresh for GENERALPOOL ISSUE End
				String temparticleIsPaper = "";
				if (articleData.getArticleIsPaper() != null
						&& "Yes".equalsIgnoreCase(articleData
								.getArticleIsPaper())) {
					temparticleIsPaper = "Y";
				} else {
					temparticleIsPaper = "N";
				}

				getSimpleJdbcTemplate()
						.update(query,
								new Object[] {

										articleData.getArticleTitle(),
										articleData.getArticleSubject(),
										articleData.getKeyWords(),
										Integer.parseInt(articleData
												.getOwningBusinessSegment()),
										temparticleIsPaper
										/* articleData.getArticleIsPaper() */,
										articleData.getArticleAbstract(),
										Integer.parseInt(articleData
												.getGeDocumentClassNumber()),
										articleData.getReferences(),
										articleData.getIpIssue(),
										articleData.getIpTag(),
										articleData.getProblemOrPurpose(),
										articleData.getAssumptions(),
										articleData.getResults(),
										articleData.getConclusions(),
										articleData.getArticleAuthors(),
										articleData.getSecOwnerId(),
										articleData.getLegacyArticleNumber(),
										userName,
										articleData.getGeneralpool(),
										articleData.getDrNumber(),
										articleData.getAiNumber(),
										// Added for Use Case 03: Add Properties
										// field for engine/product like it is
										// in DR
										articleData.getEngineModelId(),
										articleData.getReviewAreaId(),
										articleData.getDisciplineAreaId(),
										articleData.getArtErrortype(),
										Integer.parseInt(articleData
												.getArticleSeqid()) });

				LOG.info("Record unique id:" + articleData.getRecUniqueId());
				
				LOG.info("articleData.getArtErrortype():" + articleData.getArtErrortype());

				String projectSeqId = null;
				if (articleData.getOdaPjctNosId() != null) {
					projectSeqId = articleData.getOdaPjctNosId();
				} else if (articleData.getFaaPjctIdList() != null) {
					projectSeqId = articleData.getFaaPjctIdList();
				}
				if (articleData.getFaaFileCodeIdList() == null
						|| "".equalsIgnoreCase(articleData
								.getFaaFileCodeIdList())) {
					articleData.setFaaFileCodeIdList("0");
				}
				if (articleData.getRceNamesIdList() == null
						|| "".equalsIgnoreCase(articleData.getRceNamesIdList())) {
					articleData.setRceNamesIdList("0");
				}
				if (articleData.getRspIdList() == null
						|| "".equalsIgnoreCase(articleData.getRspIdList())) {
					articleData.setRspIdList("0");
				}
				if (articleData.getFormNoIdList() == null
						|| "".equalsIgnoreCase(articleData.getFormNoIdList())) {
					articleData.setFormNoIdList("0");
				}
				if (articleData.getRecTypesIdList() == null
						|| "".equalsIgnoreCase(articleData.getRecTypesIdList())) {
					articleData.setRecTypesIdList("0");
				}
				if (articleData.getFaaOdaFuncIdList() == null
						|| "".equalsIgnoreCase(articleData
								.getFaaOdaFuncIdList())) {
					articleData.setFaaOdaFuncIdList("0");
				}
				if (articleData.getRoleSeqId() == null
						|| "".equalsIgnoreCase(articleData.getRoleSeqId())) {
					articleData.setRoleSeqId("0");
				}

				LOG.info("Before new insert query call articleSeqid..."
						+ articleData.getArticleSeqid());
				LOG.info("Before new insert query call faaFileCode..."
						+ articleData.getFaaFileCodeIdList());
				LOG.info("Before new insert query call oda pjct no..."
						+ articleData.getOdaPjctNosId());
				LOG.info("Before new insert query call rec type..."
						+ articleData.getRecTypesIdList());
				LOG.info("Before new insert query call rec source id..."
						+ articleData.getRecSourceIdList());
				LOG.info("Before new insert query call rce names id..."
						+ articleData.getRceNamesIdList());
				LOG.info("Before new insert query call rsp id..."
						+ articleData.getRspIdList());
				LOG.info("Before new insert query call form no..."
						+ articleData.getFormNoIdList());
				LOG.info("Before new insert query call owner SSO..."
						+ articleData.getArticleOwnerSSO());
				LOG.info("Before new insert query call Cawc Doc no..."
						+ articleData.getCawcDocNum());
				LOG.info("Before new insert query call CCL items..."
						+ articleData.getCclIdList());
				LOG.info("Before new insert query call Unique id..."
						+ articleData.getRecUniqueId());
				LOG.info("Before new insert query call FAA Function code..."
						+ articleData.getFaaOdaFuncIdList());
				LOG.info("Before new insert query call projectSeqId..."
						+ projectSeqId);
				LOG.info("Before new insert query call role Seq Id..."
						+ articleData.getRoleSeqId());

				getSimpleJdbcTemplate()
						.update(odaUpdateQuery,
								new Object[] {
										Integer.parseInt(articleData
												.getFaaFileCodeIdList()),
										Integer.parseInt(articleData
												.getRecTypesIdList()),
										Integer.parseInt(projectSeqId),
										Integer.parseInt(articleData
												.getRecSourceIdList()),
										articleData.getCawcDocNum(),
										Integer.parseInt(articleData
												.getRceNamesIdList()),
										Integer.parseInt(articleData
												.getRoleSeqId()), // should be
																	// replaced
																	// with role
																	// seq id
										Integer.parseInt(articleData
												.getRspIdList()),
										Integer.parseInt(articleData
												.getFaaOdaFuncIdList()),
										// Integer.parseInt(articleData.getAtaNosIdList()),
										articleData.getCclIdList(),
										// articleData.getCclItms(),
										Integer.parseInt(articleData
												.getFormNoIdList()),
										articleData.getOdaManRevId(),
										articleData.getOdaAuthId(),
										articleData.getArticleOwnerSSO(),
										// articleData.getArticleOwnerSSO(),
										Integer.parseInt(articleData
												.getArticleSeqid()),
										// articleData.getRecUniqueId(),
										// articleData.getRecordActionCode(),
										Integer.parseInt(articleData
												.getArticleSeqid()), });
				System.out.println("articleData.getRecordActionCode()------->"
						+ articleData.getRecordActionCode());

				// Added for capturing GE Classification
				if (!oldDataClassName.equalsIgnoreCase(newdataClassName)) {
					articleData.setOldValue(oldDataClassName);
					articleData.setNewValue(newdataClassName);
					articleData
							.setChangedAttributeName("DocumentClassification");
					// Modified by Pradeep on 05/02/2011 for Defect Id: 1495
					articleData.setArticleSeqId(Integer.parseInt(articleData
							.getArticleSeqid()));
					captureArticlesHistory(articleData);
					// //Log.info("the status" + histStatus);
				}

				/*******/
				/*******/

			}

			else {
				if ("Y".equalsIgnoreCase(articleData.getExportInd())) {
					if ("Y".equalsIgnoreCase(articleData.getEctEnforced())) {
						articleData.setExportControlTag(null);

					}
				}

				if ("reviseArticle".equalsIgnoreCase(articleData
						.getButtonName())) {
					// LOG.info("*********reviseArticle*********");
					articleData.setButtonName(null);
					articleData.setArticleReviseInd(false);
					prevArtSeqId = articleData.getArticleSeqid();
					Map out = new HashMap();

					String temparticleIsPaper = "";
					if (articleData.getArticleIsPaper() != null
							&& "Yes".equalsIgnoreCase(articleData
									.getArticleIsPaper())) {
						temparticleIsPaper = "Y";
					} else {
						temparticleIsPaper = "N";
					}
					/**
					 * Start: Modified by Kanoj for Enhancement No: 147
					 */
					out = _createArticlePrc.executeReviseArticle(Integer
							.parseInt(articleData.getArticleSeqid()),
							articleData.getArticleTitle(), articleData
									.getArticleOldTitle(), articleData
									.getArticleSubject(), articleData
									.getArticleOwnerSSO(), articleData
									.getSecOwnerId(), Integer
									.parseInt(articleData.getFolderSeqId()),
							articleData.getKeyWords(), articleData
									.getOwningBusinessSegment(), articleData
									.getArticleAbstract(),
							temparticleIsPaper/*
											 * articleData.getArticleIsPaper ()
											 */, articleData
									.getExportControlTag(), Integer
									.parseInt(articleData
											.getGeDocumentClassNumber()),
							articleData.getReferences(), articleData
									.getIpIssue(), articleData.getIpTag(),
							articleData.getProblemOrPurpose(), articleData
									.getAssumptions(),
							articleData.getResults(), articleData
									.getConclusions(), articleData
									.getLegacyArticleNumber(), articleData
									.getArticleAuthors(), articleData
									.getArticleOwner(), articleData
									.getArticleNumber(), articleData
									.getDrNumber(), articleData.getAiNumber(),
							articleData.getEngineModelId(), articleData
									.getReviewAreaId(), articleData
									.getDisciplineAreaId(),
					articleData.getArtErrortype());
					/**
					 * End: Modified by Kanoj for Enhancement No: 147
					 */

					// Log.info("the article number 4~7ios >>>>>>>>> " +
					// (String) out.get("articleSeqIdOut"));
					articleData.setArticleSeqid((String) out
							.get("articleSeqIdOut"));

					String projectSeqId = null;
					if (articleData.getOdaPjctNosId() != null) {
						projectSeqId = articleData.getOdaPjctNosId();
					} else if (articleData.getFaaPjctIdList() != null) {
						projectSeqId = articleData.getFaaPjctIdList();
					}
					LOG.info("projectSeqId..." + projectSeqId);
					if (articleData.getFaaFileCodeIdList() == null
							|| "".equalsIgnoreCase(articleData
									.getFaaFileCodeIdList())) {
						articleData.setFaaFileCodeIdList("0");
					}
					if (articleData.getRceNamesIdList() == null
							|| "".equalsIgnoreCase(articleData
									.getRceNamesIdList())) {
						articleData.setRceNamesIdList("0");
					}
					if (articleData.getRspIdList() == null
							|| "".equalsIgnoreCase(articleData.getRspIdList())) {
						articleData.setRspIdList("0");
					}
					if (articleData.getFormNoIdList() == null
							|| "".equalsIgnoreCase(articleData
									.getFormNoIdList())) {
						articleData.setFormNoIdList("0");
					}
					if (articleData.getRecTypesIdList() == null
							|| "".equalsIgnoreCase(articleData
									.getRecTypesIdList())) {
						articleData.setRecTypesIdList("0");
					}
					if (articleData.getFaaOdaFuncIdList() == null
							|| "".equalsIgnoreCase(articleData
									.getFaaOdaFuncIdList())) {
						articleData.setFaaOdaFuncIdList("0");
					}
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.INSERT_ODARR_NEWFIELDS,
							articleData.getArticleSeqid(),
							articleData.getFaaFileCodeIdList(),
							articleData.getRecTypesIdList(),
							projectSeqId// Can be either FAA Project Number or
										// ODA Project Number
							,
							articleData.getRecSourceIdList(),
							articleData.getCawcDocNum(),
							// argArticleData.getRecordActionCode(),
							articleData.getRceNamesIdList(),
							articleData.getRoleSeqId(),// Added for testing
														// purpse.change laetr
							articleData.getRspIdList(),
							articleData.getFaaOdaFuncIdList(),
							articleData.getCclIdList(),
							// articleData.getCclItms(),
							articleData.getFormNoIdList(),
							articleData.getFormNoIdList(),// ODA_MAN_REV_SEQ_ID
							articleData.getFormNoIdList(),// ODA_AUTH_SEQ_ID
							articleData.getArticleOwnerSSO(),
							articleData.getArticleOwnerSSO(),
							articleData.getArticleSeqid());
					// articleData.getRecUniqueId());

					// mailing to notification list
					String getUserQuery = DRBQueryConstants.ARTICLE_CREATE_ARTICLE_GETNOTIFICATIONLIST_QUERY;
					RowMapper getGeneralRowMapper = new RowMapper() {
						public String mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							String email = rs.getString(1);
							return email;
						}
					};
					emailIds = getJdbcTemplate().query(
							getUserQuery,
							new Object[] { Integer.parseInt(articleData
									.getArticleSeqid()) }, getGeneralRowMapper);
					// added by santosh on 5 dec start for revision mail
					UserData userObj = (UserData) (DRBUtils
							.getServletSession(false).getAttribute("USER_DATA"));
					emailIds.add(userObj.getUserEMailID());
					// LOG.info("*********userObj eamil id*******"+emailIds.size());
					if (emailIds != null && emailIds.size() > 0) {
						// LOG.info("*******before versionChangeEmail*******");
						this.versionChangeEmail(prevArtSeqId, emailIds,
								articleData.getArticleState(), articleData);
						// LOG.info("*******after versionChangeEmail*******");
					}
					viewArticleProc.executeViewArticle(
							Integer.parseInt(articleData.getArticleSeqid()),
							articleData.getArticleOwnerSSO(), "ALL",
							Integer.parseInt(articleData.getFolderSeqId()),
							articleData, "false");

				} else if ("articleMetaData".equalsIgnoreCase(articleData
						.getButtonName())) { // Draft mode
					Map out = null;
					String temparticleIsPaper = "";
					if (articleData.getArticleIsPaper() != null
							&& "Yes".equalsIgnoreCase(articleData
									.getArticleIsPaper())) {
						temparticleIsPaper = "Y";
					} else {
						temparticleIsPaper = "N";
					}

					/**
					 * Start: Modified by Kanoj for Enhancement No: 147
					 */
					LOG.info("Before  main insert,article_seq_id ="
							+ articleData.getArticleSeqid());
					LOG.info("Before  main insert,record identifier = "
							+ articleData.getRecUniqueId());
					try {
						out = _createArticlePrc
								.executeCreateArticle(
										Integer.parseInt(articleData
												.getArticleSeqid()),
										articleData.getArticleTitle(),
										articleData.getArticleOldTitle(),
										articleData.getArticleSubject(),
										articleData.getArticleOwnerSSO(),
										articleData.getSecOwnerId(), Integer
												.parseInt(articleData
														.getFolderSeqId()),
										articleData.getKeyWords(),
										Integer.parseInt(articleData
												.getOwningBusinessSegment()),
										articleData.getArticleAbstract(),
										temparticleIsPaper/*
														 * articleData.
														 * getArticleIsPaper()
														 */, articleData
												.getExportControlTag(),
										Integer.parseInt(articleData
												.getGeDocumentClassNumber()),
										articleData.getReferences(),
										articleData.getIpIssue(), articleData
												.getIpTag(), articleData
												.getProblemOrPurpose(),
										articleData.getAssumptions(),
										articleData.getResults(), articleData
												.getConclusions(), articleData
												.getLegacyArticleNumber(),
										articleData.getArticleAuthors(),
										articleData.getArticleOwner(),
										articleData.getArticleNumber(),
										articleData.getDrNumber(), articleData
												.getAiNumber(), articleData
												.getEngineModelId(),
										articleData.getReviewAreaId(),
										articleData.getDisciplineAreaId(),
										articleData.getArtErrortype());

						articleData.setArticleSeqid(((String) out
								.get("articleSeqIdOut")));
						articleData.setArticleNumber((String) out
								.get("articleCodeOut"));

						// New insert query for ODA_REC_INFO table, - 307009315

						String projectSeqId = null;
						if (articleData.getOdaPjctNosId() != null) {
							projectSeqId = articleData.getOdaPjctNosId();
						} else if (articleData.getFaaPjctIdList() != null) {
							projectSeqId = articleData.getFaaPjctIdList();
						}
						LOG.info("projectSeqId..." + projectSeqId);
						if (projectSeqId == null) {
							projectSeqId = "0";
						}
						if (articleData.getFaaFileCodeIdList() == null
								|| "".equalsIgnoreCase(articleData
										.getFaaFileCodeIdList())) {
							articleData.setFaaFileCodeIdList("0");
						}
						if (articleData.getRceNamesIdList() == null
								|| "".equalsIgnoreCase(articleData
										.getRceNamesIdList())) {
							articleData.setRceNamesIdList("0");
						}
						if (articleData.getRspIdList() == null
								|| "".equalsIgnoreCase(articleData
										.getRspIdList())) {
							articleData.setRspIdList("0");
						}
						if (articleData.getFormNoIdList() == null
								|| "".equalsIgnoreCase(articleData
										.getFormNoIdList())) {
							articleData.setFormNoIdList("0");
						}
						if (articleData.getRecTypesIdList() == null
								|| "".equalsIgnoreCase(articleData
										.getRecTypesIdList())) {
							articleData.setRecTypesIdList("0");
						}
						if (articleData.getFaaOdaFuncIdList() == null
								|| "".equalsIgnoreCase(articleData
										.getFaaOdaFuncIdList())) {
							articleData.setFaaOdaFuncIdList("0");
						}
						if (articleData.getFormNoIdList() == null
								|| "".equalsIgnoreCase(articleData
										.getFormNoIdList())) {
							articleData.setFormNoIdList("0");
						}
						if (articleData.getRoleSeqId() == null
								|| "".equalsIgnoreCase(articleData
										.getRoleSeqId())) {
							articleData.setRoleSeqId("0");
						}

						LOG.info("Before new insert query call articleSeqid..."
								+ articleData.getArticleSeqid());
						LOG.info("Before new insert query call faaFileCode..."
								+ articleData.getFaaFileCodeIdList());
						LOG.info("Before new insert query call oda pjct no..."
								+ articleData.getOdaPjctNosId());
						LOG.info("Before new insert query call rec type..."
								+ articleData.getRecTypesIdList());
						LOG.info("Before new insert query call rec source id..."
								+ articleData.getRecSourceIdList());
						LOG.info("Before new insert query call rce names id..."
								+ articleData.getRceNamesIdList());
						LOG.info("Before new insert query call rsp id..."
								+ articleData.getRspIdList());
						LOG.info("Before new insert query call form no..."
								+ articleData.getFormNoIdList());
						LOG.info("Before new insert query call owner SSO..."
								+ articleData.getArticleOwnerSSO());
						LOG.info("Before new insert query call Cawc Doc no..."
								+ articleData.getCawcDocNum());
						LOG.info("Before new insert query call CCL items..."
								+ articleData.getCclIdList());
						LOG.info("Before new insert query call Unique id..."
								+ articleData.getRecUniqueId());
						LOG.info("Before new insert query call FAA Function code..."
								+ articleData.getFaaOdaFuncIdList());
						LOG.info("Before new insert query call projectSeqId-->"
								+ projectSeqId);

						getSimpleJdbcTemplate()
								.update(DRBQueryConstants.INSERT_ODARR_NEWFIELDS,
										Integer.parseInt(articleData
												.getArticleSeqid()),
										Integer.parseInt(articleData
												.getFaaFileCodeIdList()),
										Integer.parseInt(articleData
												.getRecTypesIdList()),
										Integer.parseInt(projectSeqId)// Can be
																		// either
																		// FAA
																		// Project
																		// Number
																		// or
																		// ODA
																		// Project
																		// Number
										,
										Integer.parseInt(articleData
												.getRecSourceIdList()),
										articleData.getCawcDocNum(),
										// argArticleData.getRecordActionCode(),
										Integer.parseInt(articleData
												.getRceNamesIdList()),
										Integer.parseInt(articleData
												.getRoleSeqId()),// Added for
																	// testing
																	// purpse.change
																	// laetr
										Integer.parseInt(articleData
												.getRspIdList()),
										Integer.parseInt(articleData
												.getFaaOdaFuncIdList()),
										articleData.getCclIdList(),
										// articleData.getCclItms(),
										Integer.parseInt(articleData
												.getFormNoIdList()),
										articleData.getOdaManRevId(),// ODA_MAN_REV_SEQ_ID
										articleData.getOdaAuthId(),// ODA_AUTH_SEQ_ID
										articleData.getArticleOwnerSSO(),
										articleData.getArticleOwnerSSO(),
										Integer.parseInt(articleData
												.getArticleSeqid()));
						// articleData.getRecUniqueId());

					} catch (Exception e) {
						System.out.println("eror");
						e.printStackTrace();
					}
					// Commented by 307009315 - 7 Nov 13
					/*
					 * String submodelNames = null; List selectedSubmodels = new
					 * ArrayList(); int index =0;
					 * 
					 * if(articleData.getEngSubModels()!=null){
					 * 
					 * List<SelectItem> submodelList =
					 * articleData.getEngSubModels();
					 * 
					 * for(SelectItem item : submodelList){
					 * LOG.info("submodel list elements--->" + item.getValue());
					 * //selectedSubmodels.add(item.getLabel());
					 * if((Integer)item.getValue()!=0){
					 * selectedSubmodels.add(index, item.getValue()); } index++;
					 * } }
					 * 
					 * 
					 * if(selectedSubmodels!=null){ int size =
					 * selectedSubmodels.size(); for(int i=0;i<size;i++){
					 * submodelNames = selectedSubmodels.get(i).toString();
					 * getSimpleJdbcTemplate
					 * ().update(DRBQueryConstants.INSERT_ENGSUBMDLS,
					 * Integer.parseInt
					 * (articleData.getArticleSeqid()),Integer.parseInt
					 * (submodelNames),articleData.getArticleOwnerSSO(),
					 * articleData.getArticleOwnerSSO()); } }
					 */

					Map saveout = new HashMap();
					saveout = viewArticleProc.executeViewArticle(
							Integer.parseInt(articleData.getArticleSeqid()),
							articleData.getArticleOwnerSSO(), "ALL",
							Integer.parseInt(articleData.getFolderSeqId()),
							articleData, "false");
					articleData = (ArticleData) saveout.get("articleData");
				}
			}
		}

		return articleData;
	}

	public ArticleData saveEngineSubModel(ArticleData articleData)
			throws DRBCommonException {
		if ("articleMetaData".equalsIgnoreCase(articleData.getButtonName())) {
			for (int i = 0; i < articleData.getSelectedEngineSubModels().size(); i++) {
				String sql = "INSERT INTO CAWC.DRB_ENG_SUB_MODEL (ENG_SUB_MODEL_SEQ_ID,ENG_SUB_SEQ_ID,ARTICLE_SEQ_ID) VALUES (CAWC.DRB_ENG_SUB_MODEL_S.NEXTVAL,?,?)";
				getSimpleJdbcTemplate().update(sql,
						articleData.getSelectedEngineSubModels().get(i),
						articleData.getArticleSeqid());
			}
		} else if (("editArticleSave").equals(articleData.getButtonName())) {

			for (int i = 0; i <= articleData.getSelectedEngineSubModels()
					.size(); i++) {
				String query = "DELETE from CAWC.DRB_ENG_SUB_MODEL where ARTICLE_SEQ_ID=?";
				getSimpleJdbcTemplate().update(query,
						articleData.getArticleSeqid());
			}
			for (int i = 0; i < articleData.getSelectedEngineSubModels().size(); i++) {
				String sql = "INSERT INTO CAWC.DRB_ENG_SUB_MODEL (ENG_SUB_MODEL_SEQ_ID,ENG_SUB_SEQ_ID,ARTICLE_SEQ_ID) VALUES (CAWC.DRB_ENG_SUB_MODEL_S.NEXTVAL,?,?)";
				getSimpleJdbcTemplate().update(sql,
						articleData.getSelectedEngineSubModels().get(i),
						articleData.getArticleSeqid());
			}
		}
		return null;
	}

	/**
	 * @param articleNo
	 * @return ArticleData
	 * @throws DRBCommonException
	 */
	@Transactional
	public ArticleData promoteArticle(ArticleData articleData)
			throws DRBCommonException {
		/*
		 * ViewArticleProc viewArticleProc = null; ApplicationContext context =
		 * null;
		 */

		/*
		 * ServletContext _serContext = (ServletContext)
		 * FacesContext.getCurrentInstance().getExternalContext().getContext();
		 * context =
		 * WebApplicationContextUtils.getWebApplicationContext(_serContext);
		 * viewArticleProc = (ViewArticleProc)
		 * context.getBean("viewArticleProc");
		 */

		List<String> emailIds = null;
		if ("yes".equalsIgnoreCase(articleData.getMetadataEdited())) {
			if (articleData.getGeneralpool() != null
					&& "Yes".equalsIgnoreCase(articleData.getGeneralpool())) {
				articleData.setGeneralpool("Y");
			} else {
				articleData.setGeneralpool("N");
			}
		}

		UserData ownerUserObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String userName = ownerUserObj.getUserLastName()
				+ ArticleConstants.EDRB_SEPARATOR
				+ ownerUserObj.getUserFirstName();

		if (articleData.getArticleState().equalsIgnoreCase("Issued")) {
			// Log.info("articleData.getFolderSeqId()articleData.getArticleSeqid()userObj=============================>"
			// + articleData.getFolderSeqId() + "--" +
			// articleData.getArticleSeqid() + "--" +
			// ownerUserObj.getUserSSOID());

			auditStatusUpdationOnArticleIssue(
					articleData.getFolderSeqId(),
					articleData.getArticleSeqid(),
					DRBUtils.setPersonnelFullName(
							ownerUserObj.getUserLastName(),
							ownerUserObj.getUserFirstName()));

			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_ISSUE_DATE_QUERY,
							new Object[] {
									userName,
									articleData.getArticleReviewReqId(),
									Integer.parseInt(articleData
											.getArticleSeqid()) });
			articleData.setOldValue("Draft");
			articleData.setNewValue(articleData.getArticleState());
			articleData.setArticleSeqId(Integer.parseInt(articleData
					.getArticleSeqid()));
			articleData.setChangedAttributeName("Issued");
			this.captureArticlesHistory(articleData);

			if (articleData.getRevisionArticleSeqId() != null) {
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.ARTICLE_REVISE_ARTICLE_PROMOTE_ARTICLE_QUERY,
								new Object[] {
										userName,
										articleData.getArticleReviewReqId(),
										Integer.parseInt(articleData
												.getArticleSeqid()) });
				// Need to capture to history
				articleData.setOldValue("Checked-Out");
				articleData.setNewValue("Issued");
				articleData.setArticleSeqId(Integer.parseInt(articleData
						.getRevisionArticleSeqId()));
				articleData.setChangedAttributeName("Issued");
				String userId = ownerUserObj.getUserSSOID();
				getJdbcTemplate()
						.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_STATE_CHANGE_HISTORY_QUERY,
								new Object[] {
										userId,
										articleData.getChangedAttributeName(),
										articleData.getOldValue(),
										articleData.getNewValue(),
										Integer.parseInt(articleData
												.getRevisionArticleSeqId()),
										userName, userName });
			}

		} else if (("In Review")
				.equalsIgnoreCase(articleData.getArticleState())) {
			int requestCount = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.DUPLICATE_REQUESTS,
					new Object[] { articleData.getArticleSeqid(),
							articleData.getReviewerid(), "Review Article" });
			if (requestCount == 0) {
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_REVIEW_DATE_QUERY,
								new Object[] {
										userName,
										articleData.getArticleReviewReqId(),
										Integer.parseInt(articleData
												.getArticleSeqid()) });

				RowMapper getReviewerUserId = new RowMapper() {
					public List mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						List<String> revDetails = new ArrayList<String>();
						revDetails.add(rs.getString("REVIEWER_USER_ID"));
						revDetails.add(rs.getString("REALEMAIL"));
						return revDetails;
					}
				};
				List reviewerIdList = getJdbcTemplate()
						.query(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_GET_REVIEWERID,
								new Object[] { Integer.parseInt(articleData
										.getArticleSeqid()) },
								getReviewerUserId);
				reviewerIdList = (ArrayList<String>) reviewerIdList.get(0);
				if (reviewerIdList != null) {
					this.emailToReviewer(articleData.getArticleNumber(),
							(String) reviewerIdList.get(1),
							articleData.getArticleTitle(),
							articleData.getArticleEcTag(),
							articleData.getArticleSeqid());
				}
				String taskDesc = "Article#: " + articleData.getArticleNumber()
						+ " assigned for Review as Primary Reviewer";
				if (reviewerIdList != null) {
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.ASSIGN_REVIEWER_REQUEST_EVENT,
							new Object[] {
									Integer.parseInt(articleData
											.getArticleSeqid()),
									ownerUserObj.getUserSSOID(),
									reviewerIdList.get(0), taskDesc,
									articleData.getLibrarySeqId(), userName,
									userName });
				}
				// following block is added by dhiraj on 16-08-11 for change in
				// additional reviewer as suggested by mythili

				RowMapper getAdditionalReviewerUserId = new RowMapper() {
					public List mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						List<String> revDetails = new ArrayList<String>();
						revDetails.add(rs
								.getString("ADDITIONAL_REVIEWER_USER_ID"));
						revDetails.add(rs.getString("REALEMAIL"));
						return revDetails;
					}
				};
				List additionalReviewerIdList = getJdbcTemplate()
						.query(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_GET_ADDITIONAL_REVIEWERID,
								new Object[] { Integer.parseInt(articleData
										.getArticleSeqid()) },
								getAdditionalReviewerUserId);

				// Above block is commented by dhiraj on 29-08-11 and following
				// is added

				if (additionalReviewerIdList != null
						&& additionalReviewerIdList.size() > 0)

				{
					additionalReviewerIdList = (ArrayList<String>) additionalReviewerIdList
							.get(0);
					this.emailToReviewer(articleData.getArticleNumber(),
							(String) additionalReviewerIdList.get(1),
							articleData.getArticleTitle(),
							articleData.getArticleEcTag(),
							articleData.getArticleSeqid());
				}

				String taskDesc1 = "Article#: "
						+ articleData.getArticleNumber()
						+ " assigned for Review as Additional Reviewer";

				if (additionalReviewerIdList != null
						&& additionalReviewerIdList.size() > 0) {
					getSimpleJdbcTemplate()
							.update(DRBQueryConstants.ADD_ADDITIONAL_REVIEWER_REQUEST_EVENT,
									new Object[] {
											Integer.parseInt(articleData
													.getArticleSeqid()),
											ownerUserObj.getUserSSOID(),
											additionalReviewerIdList.get(0),
											taskDesc1,
											articleData.getLibrarySeqId(),
											userName, userName });
				}
				// End of block added by dhiraj on 16-08-11 for change in
				// additional reviewer as suggested by mythili

				articleData.setOldValue("Draft");
				articleData.setNewValue(articleData.getArticleState());
				articleData.setArticleSeqId(Integer.parseInt(articleData
						.getArticleSeqid()));
				articleData.setChangedAttributeName("Enter review");
				/* Venkat updated on 01/01/2010 for Defect ID: 1311 */
				this.captureArticlesHistory(articleData);
			} else {
				// Subhra 17-DEC-2010
				viewArticleProc.executeViewArticle(
						Integer.parseInt(articleData.getArticleSeqid()),
						articleData.getArticleOwnerSSO(), "ALL",
						Integer.parseInt(articleData.getFolderSeqId()),
						articleData, "false");
				return articleData;
			}
		}
		String getUserQuery = DRBQueryConstants.ARTICLE_CREATE_ARTICLE_GETNOTIFICATIONLIST_QUERY;
		RowMapper getGeneralRowMapper = new RowMapper() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String email = rs.getString(1);
				return email;
			}
		};
		emailIds = getJdbcTemplate()
				.query(getUserQuery,
						new Object[] { Integer.parseInt(articleData
								.getArticleSeqid()) }, getGeneralRowMapper);

		/**
		 * Commented by Pradeep for Enhancement No: 64
		 */
		try {
			if ((String) ownerUserObj.getUserEMailID()/* .toString() */!= null) {
				emailIds.add((String) ownerUserObj.getUserEMailID()/*
																	 * .toString(
																	 * )
																	 */);// Code
																			// Review
																			// Changed
																			// By
																			// Naresh
																			// on
																			// 22DEC10
			}
		} catch (Exception e) {
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);// Code
																		// Review
																		// Changed
																		// By
																		// Naresh
																		// on
																		// 22DEC10
		}
		if (emailIds != null && emailIds.size() > 0) {
			if ("Issued".equalsIgnoreCase(articleData.getArticleState())) {
				this.stateChangeEmail(articleData.getArticleNumber(), emailIds,
						articleData.getArticleState(), articleData);
			}
		}
		viewArticleProc.executeViewArticle(
				Integer.parseInt(articleData.getArticleSeqid()),
				articleData.getArticleOwnerSSO(), "ALL",
				Integer.parseInt(articleData.getFolderSeqId()), articleData,
				"false");
		return articleData;

	}

	public boolean getRole(String libSeqId, String reviewId) {
		boolean flag = false;
		try {
			String saveModifiedDataQuery = "SELECT COUNT(USER_DATA_ACCESS_SEQ_ID) COUNT FROM "
					+ "CAWC.DRB_USER_DATA_ACCESS WHERE APP_TABLE_SEQ_ID = (SELECT APP_TABLE_SEQ_ID FROM CAWC.DRB_APP_TABLE "
					+ "WHERE APP_TABLE_NAME = 'DRB_LIBRARY')  AND USER_ID = ? AND APP_DATA_PK_ID = ? AND "
					+ "ACCESS_TYPE_SEQ_ID = (SELECT ACCESS_TYPE_SEQ_ID FROM CAWC.DRB_ACCESS_TYPE WHERE UPPER(ACCESS_TYPE_NAME) = 'REVIEWER')";
			String count = (String) getSimpleJdbcTemplate().queryForObject(
					saveModifiedDataQuery, new getCountFlag(),
					new Object[] { reviewId, libSeqId });
			if (!count.equals("0")) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (Exception e) {
			e.getMessage();
		}
		return flag;
	}

	/***
	 * The getArticleMetadata(() is used to get the Article Metadata to the
	 * particular article number from the database
	 * 
	 * @param articleseqId
	 * @return ArticleData
	 * @throws DRBCommonException
	 */
	public ArticleData getArticleMetadata(String articleseqId,
			String buttonName, String checkedNamedAccess)
			throws DRBCommonException {

		/*
		 * ViewArticleProc viewArticleProc = null; ApplicationContext context =
		 * null; ServletContext _serContext = (ServletContext)
		 * FacesContext.getCurrentInstance().getExternalContext().getContext();
		 * context =
		 * WebApplicationContextUtils.getWebApplicationContext(_serContext);
		 * viewArticleProc = (ViewArticleProc)
		 * context.getBean("viewArticleProc");
		 */

		ArticleData articleData = new ArticleData();
		// Log.info("getArticleMetadata :: articleseqId :: " + articleseqId);
		if (null != buttonName && "reviseArticle".equals(buttonName)) {

			UserData ownerUserObj = (UserData) (DRBUtils
					.getServletSession(false)
					.getAttribute(ArticleConstants.SESSION_USER_DATA));
			viewArticleProc.executeViewArticle(Integer.parseInt(articleseqId),
					ownerUserObj.getUserSSOID(), "ALL", 0, articleData,
					checkedNamedAccess);
		} else {

			articleData.setShowPreviousLinks("");
			articleData.setSuccessresult("");
			articleData.setSuccessresult("");
			String userId = "";
			UserData userObj = (UserData) (DRBUtils.getServletSession(false)
					.getAttribute(ArticleConstants.SESSION_USER_DATA));
			userId = userObj.getUserSSOID();
			viewArticleProc.executeViewArticle(Integer.parseInt(articleseqId),
					userId, "ALL", 0, articleData, checkedNamedAccess);
		}
		/*
		 * Start: Added for Use Case 03: Add Properties field for engine/product
		 * like it is in DR
		 */
		articleData = getPropertyList(articleData);
		/*
		 * End: Added for Use Case 03: Add Properties field for engine/product
		 * like it is in DR
		 */

		List<ArticleData> articleDataLs = null;
		if (articleData.getArticleNumber() == null) {
			String query = "select ART.ARTICLE_CODE,LIB.ECT_ENFORCED_IND from CAWC.DRB_ARTICLE ART , CAWC.DRB_LIBRARY LIB "
					+ "WHERE ART.LIBRARY_SEQ_ID=LIB.LIBRARY_SEQ_ID AND ART.ARTICLE_SEQ_ID=?";
			articleDataLs = getSimpleJdbcTemplate().query(query,
					new ParameterizedRowMapper<ArticleData>() {
						public ArticleData mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							ArticleData articleData = new ArticleData();
							articleData.setArticleNumber(rs
									.getString("ARTICLE_CODE"));
							articleData.setEctEnforced(rs
									.getString("ECT_ENFORCED_IND"));
							return articleData;
						}
					}, Integer.parseInt(articleseqId));
		}
		if (!DRBUtils.isEmptyList(articleDataLs)) {
			articleData.setArticleNumber(articleDataLs.get(0)
					.getArticleNumber());
			articleData.setEctEnforced(articleDataLs.get(0).getEctEnforced());
		}
		return articleData;
	}

	/**
	 * This Method is used to fetch the Article Related Meta-data
	 * 
	 * @param articleNo
	 * @return Object
	 * @throws DRBCommonException
	 */
	public String getArticleMetadataTitle(String articleNo)
			throws DRBCommonException {
		String saveModifiedDataQuery = "SELECT REVISION_ARTICLE_SEQ_ID FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID=? ";
		String newTitle = null;
		newTitle = (String) getSimpleJdbcTemplate().queryForObject(
				saveModifiedDataQuery, new getTitle(),
				new Object[] { articleNo });
		return newTitle;
	}

	static class getTitle implements ParameterizedRowMapper<String> {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String countValue = rs.getString("REVISION_ARTICLE_SEQ_ID");
			return countValue;
		}
	}

	/**
	 * This Method is used to fetch the Article Related Meta-data
	 * 
	 * @param articleNo
	 * @return Object
	 * @throws DRBCommonException
	 */
	public String getArticleMetadataPreRivisionId(String articleNo)
			throws DRBCommonException {
		String saveModifiedDataQuery = "SELECT ARTICLE_TITLE FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID=? ";
		String newTitle = null;
		newTitle = (String) getSimpleJdbcTemplate().queryForObject(
				saveModifiedDataQuery, new getRevisionTitle(),
				new Object[] { articleNo });
		return newTitle;
	}

	static class getRevisionTitle implements ParameterizedRowMapper<String> {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			// Log.info("the rs.getString(COUNT) is getRole$$$$$$-----------" +
			// rs.getString("COUNT"));
			String countValue = rs.getString("ARTICLE_TITLE");
			// Log.info("the countValue is getRole$$$$$$-----------"+countValue);
			return countValue;
		}

	}

	/**
	 * @param articleCode
	 * @param articleSeqId
	 * @param demotionReason
	 * @param userID
	 * @throws DRBCommonException
	 */
	public void sendDemoteArticleMail(String sexportStatus, String artTitle,
			String articleCode, String articleSeqId, String demotionReason,
			String userName) throws DRBCommonException {
		try {
			// Log.info("************** S T A R T ******************");
			String linkUrl = null;
			ArrayList<String> toMail = new ArrayList<String>();
			ArrayList<String> ccMail = new ArrayList<String>();
			List<UserData> lstUserObjs = null;
			List list;
			List result = null;
			StringBuffer subject = null;
			StringBuffer body = null;
			subject = new StringBuffer();
			body = new StringBuffer();
			ResourceBundle rbundle = ResourceBundle
					.getBundle(FolderConstants.ENV_PROP_FILE);
			linkUrl = rbundle.getString(FolderConstants.EMAIL_LINK);
			RowMapper reviewersIds = new RowMapper() {
				public List mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					List list = new ArrayList();
					list.add(rs.getString(ReportConstants.REVIEWER_USER_ID));
					list.add(rs
							.getString(ReportConstants.ADDITIONAL_REVIEWER_USER_ID));
					// added by Vasantha for getting the owner mail id of demote
					// the article
					list.add(rs.getString(ReportConstants.OWNER_USER_ID));// end
					return list;
				}
			};
			list = getJdbcTemplate().query(
					DRBQueryConstants.FETCH_REVIEWERS_ARTICLE_IDS,
					new Object[] { articleSeqId }, reviewersIds);
			if (list != null && list.size() > 0) {
				result = (List) list.get(0);

				lstUserObjs = _userServiceObj.getUserDetails(result);
				if (lstUserObjs != null) {
					for (int itr = 0; itr < lstUserObjs.size(); itr++) {
						toMail.add(itr, lstUserObjs.get(itr).getUserEMailID());
					}
				}
			}
			// Log.info("sendDemoteArticleMail() ::::::: toMail " + toMail);
			subject.append(ArticleConstants.DEMOTE_ARTICLE_BY_OWNER_SUBJECT);
			subject.append(articleCode);
			if (sexportStatus != null
					&& (sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
				subject.append(ArticleConstants.DRB_DASH);
				subject.append(artTitle);
			}
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append("The DRB article ");
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
			body.append(ArticleConstants.AHREF).append(linkUrl)
					.append("?articleSeqID=").append(articleSeqId);
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
			body.append(" target=\"new\">");
			body.append(articleCode);
			body.append("</a>");
			if (sexportStatus != null
					&& (sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
				body.append(ArticleConstants.DRB_DASH);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(articleSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(artTitle);
				body.append("</a>");
			}
			body.append(" has been demoted from review by ");
			body.append(userName);
			body.append(" back to the 'Draft' state.");
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.DEMOTE_ARTICLE_BY_OWNER_REASON);
			body.append(demotionReason + ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append("You will be notified when this article is re-submitted for your review.");
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.AUTO_GENERATED);
			body.append(ArticleConstants.TABLE_ROW_END_TAG);
			body.append(ArticleConstants.HTML_TABLE_END_TAG);
			body.append(ArticleConstants.HTML_BODY_END_TAG);
			body.append(ArticleConstants.HTML_END_TAG);
			// Log.info("************** D O N E ******************");
			_mailBean.sendMail(toMail, ccMail, subject.toString(),
					body.toString(), _mailSend, ArticleConstants.TEST);
			// Log.info("i am here befoe sending sendDemoteArticleMail" );
			body = null;// heap issue
		} catch (MailSendException e) {// Code Review Change By Naresh on
										// 22DEC10
			LOG.error("Exception[MailSendException]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception[sendDemoteArticleMail]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		}

	}

	/**
	 * @param articleNo
	 * @param amendArticle
	 * @param userId
	 * @return String
	 */
	@Transactional
	public String addAmend(String articleNo, String amendArticle, String userId)
			throws DRBCommonException {
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		StringBuffer strBfr = new StringBuffer();
		strBfr.append(userObj.getUserLastName()).append(", ")
				.append(userObj.getUserFirstName());
		String value = null;
		// Log.info(articleNo + " :: " + amendArticle + " :: " + userId);
		String amendArticleCode = ArticleConstants.EMPTY;
		String actualArticleCode = ArticleConstants.EMPTY;
		List<String> amendState = null;
		// added by santosh on 5 dec start
		List<String> notifyUserMailIds = new ArrayList<String>();
		// added by santosh on 5 dec end
		String AMEND_STATE = DRBQueryConstants.AMEND_STATE;
		amendState = getSimpleJdbcTemplate().query(AMEND_STATE,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("STATE_NAME");
					}
				}, amendArticle);
		if (amendState != null) {
			if (!amendState.get(0).equalsIgnoreCase("Issued")) {
				value = "Please select an article which is in issued state";
			} else {
				List<Integer> articleAddedORNot = null;// new
				String ARTICLE_AMEND = DRBQueryConstants.ARTICLE_AMEND;
				articleAddedORNot = getSimpleJdbcTemplate().query(
						ARTICLE_AMEND, new ParameterizedRowMapper<Integer>() {
							public Integer mapRow(ResultSet rs, int arg1)
									throws SQLException {
								return rs.getInt(1);
							}
						}, articleNo, amendArticle);
				if (articleAddedORNot != null && articleAddedORNot.get(0) == 0) {
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.ADD_AMMENDS, articleNo,
							amendArticle, userId, strBfr.toString(),
							strBfr.toString());
					amendArticleCode = getSimpleJdbcTemplate().queryForObject(
							DRBQueryConstants.ART_CODE_HIST,
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int arg1)
										throws SQLException {
									return rs.getString("ARTICLE_CODE");
								}
							}, amendArticle);
					// For capturing history for amends
					ArticleData articleData = new ArticleData();
					articleData.setChangedAttributeName("Amends");
					articleData.setOldValue("");
					articleData.setNewValue(amendArticleCode);
					articleData.setArticleSeqid(articleNo);
					captureArticlesHistory(articleData);
					// For capturing history for amended
					actualArticleCode = getSimpleJdbcTemplate().queryForObject(
							DRBQueryConstants.ART_CODE_HIST,
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int arg1)
										throws SQLException {
									return rs.getString("ARTICLE_CODE");
								}
							}, articleNo);
					ArticleData articleData1 = new ArticleData();
					articleData1.setChangedAttributeName("Amended");
					articleData1.setOldValue("");
					articleData1.setNewValue(actualArticleCode);
					articleData1.setArticleSeqid(amendArticle);
					captureArticlesHistory(articleData1);
					// added by santosh on 5 dec start
					String articleTitleAmends = articleData1.getArticleTitle();
					// added by santosh on 31 jan for amend article mail
					String amendArticleOwnerEmail = getSimpleJdbcTemplate()
							.queryForObject(
									DRBQueryConstants.AMEND_ARTICLE_OWNER_EMAIL,
									new ParameterizedRowMapper<String>() {
										public String mapRow(ResultSet rs,
												int arg1) throws SQLException {
											return rs.getString("REALEMAIL");
										}
									}, amendArticle);

					notifyUserMailIds.add(amendArticleOwnerEmail);
					List<String> viewCheckList = getSimpleJdbcTemplate().query(
							DRBQueryConstants.AMEND_ARTICLE_NOTIFIEYS,
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									rs.getString("NOTIFY_USER_ID");
									return rs.getString("NOTIFY_USER_ID");
								}
							}, Integer.parseInt(amendArticle));

					try {
						int counter = 0;
						if (viewCheckList != null && !viewCheckList.isEmpty()) {
							for (counter = 0; counter < viewCheckList.size(); counter++) {

								String amendArticleNotifierEmail = getSimpleJdbcTemplate()
										.queryForObject(
												DRBQueryConstants.AMEND_ARTICLE_NOTIFY_EMAIL,
												new ParameterizedRowMapper<String>() {
													public String mapRow(
															ResultSet rs,
															int arg1)
															throws SQLException {
														return rs
																.getString("REALEMAIL");
													}
												}, viewCheckList.get(counter));
								notifyUserMailIds
										.add(amendArticleNotifierEmail);

							}
						}
					} catch (Exception e) {
						LOG.error(e);
					}

					// added by santosh start
					// LOG.info("*****Amends article dao impl before mail*****articleTitleAmends***"+articleTitleAmends);
					String changedAttribute = articleData
							.getChangedAttributeName();
					sendStateChangeEmailForAmends(amendArticleCode,
							articleTitleAmends, changedAttribute,
							notifyUserMailIds);
					// LOG.info("*****Amends article dao impl after mail********");
					// added by santosh 5 dec ends
					value = "";
				} else {
					value = "Please select another article as this article already amended";
				}

			}
		}
		return value;
	}

	// added by santosh on 5 dec strat
	// added by santosh on 5 dec start for amends
	// sendStateChangeEmailForAmends(articleNo,articleNo,articleData.getChangedAttributeName(),
	// notifyUserMailIds);
	public void sendStateChangeEmailForAmends(String articleNo,
			String amendArticle, String changedAttribute,
			List<String> notifyUserMailIds) {
		try {
			// Log.info("Article Dao IMPL  Before Sending Email to Notification list");
			String linkUrl = null;
			List<String> toMail = null;
			ArrayList<String> ccMail = new ArrayList<String>();
			StringBuffer subject = null;
			StringBuffer body = null;
			subject = new StringBuffer();
			body = new StringBuffer();
			ResourceBundle rbundle = ResourceBundle
					.getBundle(FolderConstants.ENV_PROP_FILE);
			linkUrl = rbundle.getString(FolderConstants.EMAIL_LINK);
			toMail = notifyUserMailIds;
			// LOG.info("********amend size*****"+toMail.size());
			subject.append(ArticleConstants.ARTICLE_STATE_CHANGE_EMAIL_SUBJECT_AMENDS);
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append(ArticleConstants.ARTICLE_NO_LABLE + articleNo
					+ ArticleConstants.HAS_AMENDED);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);

			body.append(ArticleConstants.CLICK_TO_GOTO_DRB_HOME);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
			body.append(ArticleConstants.AHREF).append(linkUrl);
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
			body.append(" target=\"new\">");
			body.append(ArticleConstants.DRB_HOME);
			body.append("</a>");
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);

			body.append(ArticleConstants.AUTO_GENERATED
					+ ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.TABLE_ROW_END_TAG);
			body.append(ArticleConstants.HTML_TABLE_END_TAG);
			body.append(ArticleConstants.HTML_BODY_END_TAG);
			body.append(ArticleConstants.HTML_END_TAG);

			_mailBean.sendMail(toMail, ccMail, subject.toString(),
					body.toString(), _mailSend, ArticleConstants.TEST);
			// LOG.info("Article Dao IMPL ::: After sending email to notification list");
			body = null;// heap issue
		} catch (MailSendException e) {// Code Review Change By Naresh on
										// 22DEC10
			LOG.error("Exception[MailSendException]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception[sendStateChangeEmail]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		}
	}

	// added by santosh ends on 5 dec

	// added by santosh on 5 dec ends

	/**
	 * @param selectedList
	 * @param articleNo
	 * @param userID
	 * @param email
	 * @return String
	 */
	@Transactional
	public String updateNotificationList(List<SelectItem> selectedList,
			String articleSeqId, String reviewRole) throws DRBCommonException {
		// Log.info("update NOTIFICATION LIST DAO Class=======articleSeqId===" +
		// articleSeqId);
		int updateNotification = 0;
		String str = null;
		String ssoID = null;
		String toUpdate = "";
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String loginName = userObj.getUserLastName()
				+ ArticleConstants.SEPARATOR + userObj.getUserFirstName();
		updateNotification = getSimpleJdbcTemplate().update(
				DRBQueryConstants.REMOVE_USERS_NOTIFICATION_LIST,
				Integer.parseInt(articleSeqId));
		for (int itr = 0; itr < selectedList.size(); itr++) {
			str = (String) selectedList.get(itr).getValue();
			ssoID = str.substring(str.indexOf('(') + 1, str.indexOf(')'));
			updateNotification = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_NOTIFICATION_USER_LIST, ssoID,
					Integer.parseInt(articleSeqId), loginName, loginName);
		}
		if (reviewRole != null && updateNotification > 0
				&& reviewRole.trim().endsWith(ArticleConstants.EDRB_ROLE_03)) {
			ParameterizedRowMapper<List<String>> mapper = new ParameterizedRowMapper<List<String>>() {
				public List<String> mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					List<String> list = new ArrayList<String>();
					list.add(rs.getString("REVIEWER_METADATA_EDIT_IND"));
					list.add(rs.getString("REVIEWER_METADATA_EDIT"));
					return list;
				}
			};
			List<List<String>> returnList = getSimpleJdbcTemplate().query(
					DRBQueryConstants.IS_ARTICLE_EDITED, mapper,
					new Object[] { articleSeqId });
			if (returnList != null && returnList.size() > 0) {
				String metaDataInd = returnList.get(0).get(0);
				String metaDataEdit = returnList.get(0).get(1);
				boolean updateFlag = false;
				if (metaDataEdit != null) {
					String metaDataSplit[] = metaDataEdit.split(",");
					for (int itr = 0; itr < metaDataSplit.length; itr++) {
						updateFlag = metaDataSplit[itr]
								.equalsIgnoreCase("Article Notification List");
						if (updateFlag) {
							// Log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%update flag%%%% "
							// + updateFlag);
							break;
						}
					}
				}
				// Code Review Changed By Naresh on 22DEC10 Start
				if ("".equals(metaDataEdit)) {
					metaDataEdit = "Article Notification List,";
				} else if (metaDataEdit != null && updateFlag == false) {
					metaDataEdit = metaDataEdit + "Article Notification List,";
				}
				// Log.info(metaDataInd);
				if ("".equals(metaDataInd) || "N".equalsIgnoreCase(metaDataInd)) {
					if (reviewRole
							.equalsIgnoreCase(ArticleConstants.EDRB_ROLE_03)) {
						toUpdate = "P";
					} else {
						toUpdate = "A";
					}
				} else if ("".equals(metaDataInd)
						|| "P".equalsIgnoreCase(metaDataInd)) {
					if (reviewRole
							.equalsIgnoreCase(ArticleConstants.EDRB_ROLE_03)) {
						toUpdate = "P";
					} else {
						toUpdate = "Y";
					}
				} else if ("".equals(metaDataInd)
						|| "A".equalsIgnoreCase(metaDataInd)) {
					if (reviewRole
							.equalsIgnoreCase(ArticleConstants.EDRB_ROLE_03)) {
						toUpdate = "Y";
					} else {
						toUpdate = "A";
					}
				}
				if ("".equals(metaDataInd) || "Y".equalsIgnoreCase(metaDataInd)) {
					toUpdate = "Y";
				}
				// Code Review Changed By Naresh on 22DEC10 End
				// Log.info("The Final metadata edit feild is== " +
				// metaDataEdit);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_REVIEWER_METADATA_IND,
						toUpdate, metaDataEdit, loginName, articleSeqId);
			}
		}
		return toUpdate;
	}

	/**
	 * @param articleNumber
	 * @param userRole
	 * @param action
	 * @param articleState
	 * @param userId
	 * @param ownerSSOId
	 * @param newUserName
	 * @param userName
	 * @param articleSeqId
	 * @throws DRBCommonException
	 */
	public void sendArticleUserMail(String exportStatus, String emailsIds,
			String articleNumber, String userRole, String action,
			String articleState, String userId, String ownerSSOId,
			String newUserName, String uName, String articleSeqId)
			throws DRBCommonException {
		try {
			List<String> toMail = new ArrayList<String>();
			List<String> ccMail = new ArrayList<String>();
			StringBuffer subject = new StringBuffer();
			StringBuffer body = new StringBuffer();
			String ownerEmail = null;
			String reviewerEmail = null;
			String addReviewerEmail = null;
			String userName = uName;
			String uname[] = userName.split(ArticleConstants.SEPARATOR);
			userName = uname[1] + ArticleConstants.EMPTY_WITH_SPACE + uname[0];

			List list = null;
			String mailSubject = null;
			String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			if (userRole != null
					&& ArticleConstants.EDRB_ROLE_01.equalsIgnoreCase(userRole)) {
				if (articleState != null && emailsIds != null) {
					String emailID[] = emailsIds.split(ArticleConstants.TILDA);
					if (emailID != null) {
						if (emailID.length > 0) {
							ownerEmail = emailID[0];
						}
						if (emailID.length > 1) {
							reviewerEmail = emailID[1];
						}
						if (emailID.length > 2) {
							addReviewerEmail = emailID[2];
						}

					}

					sendArticleOwnerChangeMail(ownerEmail, articleNumber,
							articleSeqId);
					if (articleState
							.equalsIgnoreCase(ArticleConstants.STATE_IN_REVIEW)) {
						mailSubject = ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_SUBJECT_REVIEWER;
						toMail.add(reviewerEmail);
						if (addReviewerEmail != null
								&& !"".equals(addReviewerEmail.trim())) {
							toMail.add(addReviewerEmail);
						}

						body.append(ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_BODY1);
						body.append(ArticleConstants.ARTICLE_NO_LABLE);
						/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
						body.append(ArticleConstants.AHREF).append(linkUrl)
								.append("?articleSeqID=").append(articleSeqId);
						/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
						body.append(" target=\"new\">");
						body.append(articleNumber);
						body.append("</a>");
						body.append(" "
								+ ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_BODY4
								+ " " + newUserName);
						body.append(ArticleConstants.EDRB_DOT_STRING);
					}
				}
			} else {
				sendMailToOwner(articleNumber, userRole, userName, newUserName,
						ownerSSOId, articleSeqId);
				RowMapper getArticleOwnerRowMapper = new RowMapper() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						String eMail = rs.getString("REALEMAIL");
						return eMail;
					}
				};
				list = getJdbcTemplate().query(DRBQueryConstants.GET_EMAIL_ID,
						new Object[] { userId }, getArticleOwnerRowMapper);
				if (list != null && list.size() > 0) {
					if (list.get(0) != null) {
						String addReviewerMailId = list.get(0).toString();
						toMail.add(addReviewerMailId);
					}
				}
				body.append(ArticleConstants.ARTICLE_NO_LABLE + " ");
				body.append(articleNumber);
				if (userRole != null
						&& ArticleConstants.EDRB_ROLE_03
								.equalsIgnoreCase(userRole)) {
					mailSubject = ArticleConstants.REASSIGN_REVIEWER_EMAIL_SUBJECT_FOR_REVIEWER;
					body.append(" " + ArticleConstants.REVIEWER_EMAIL_BODY_01
							+ " " + userName);
					body.append(ArticleConstants.EDRB_DOT_STRING);
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					body.append(ArticleConstants.REVIEWER_EMAIL_BODY_02);
					body.append(ArticleConstants.EDRB_DOT_STRING);

				} else if (userRole != null
						&& ArticleConstants.EDRB_ROLE_04
								.equalsIgnoreCase(userRole)) {
					mailSubject = ArticleConstants.ADDITIONAL_REVIEWER_EMAIL_SUBJECT_FOR_REVIEWER;
					body.append(" "
							+ ArticleConstants.ADD_REVIEWER_EMAIL_BODY_01 + " "
							+ userName);
					body.append(ArticleConstants.EDRB_DOT_STRING);
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					body.append(ArticleConstants.REVIEWER_EMAIL_BODY_02);
					body.append(ArticleConstants.EDRB_DOT_STRING);
				}
			}
			body.append(ArticleConstants.TABLE_ROW_END_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append("Please click on the below link to navigate to My Tasks.");
			body.append("<br>");
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
			body.append(ArticleConstants.AHREF).append(linkUrl)
					.append("?emailMyTask=MYTASK");
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
			body.append(" target=\"new\">");
			body.append("My tasks");
			body.append("</a>");
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.AUTO_GENERATED);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.HTML_TABLE_END_TAG);
			body.append(ArticleConstants.HTML_BODY_END_TAG);
			body.append(ArticleConstants.HTML_END_TAG);
			subject.append(mailSubject);
			subject.append(articleNumber);
			if (exportStatus != null
					&& (exportStatus.equalsIgnoreCase(ArticleConstants.DRB_NLR) || exportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
				subject.append(ArticleConstants.DRB_DASH);
				String IS_ARTICLE = DRBQueryConstants.ARTCLE_ID;
				List<String> isfolderID = getSimpleJdbcTemplate().query(
						IS_ARTICLE, new ParameterizedRowMapper<String>() {
							public String mapRow(ResultSet rs, int arg1)
									throws SQLException {
								String edited = rs.getString("ARTICLE_TITLE");
								return edited;
							}
						}, articleSeqId);

				String articleTitle = isfolderID.get(0);
				subject.append(articleTitle);
			}
			_mailBean.sendMail(toMail, ccMail, subject.toString(),
					body.toString(), _mailSend, ArticleConstants.TEST);
			body = null;// heap issue
		} catch (MailSendException e) {
			LOG.error("Exception[MailSendException]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception[sendArticleUserMail]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		}
	}

	/**
	 * @param articleNumber
	 * @param userRole
	 * @param userName
	 * @param newUserName
	 * @param ownerSSOId
	 * @throws DRBCommonException
	 */
	public void sendMailToOwner(String articleNumber, String userRole,
			String userName, String newUserName, String ownerSSOId,
			String articleSeqId) throws DRBCommonException {

		List<String> toMail = new ArrayList<String>();
		List<String> ccMail = new ArrayList<String>();
		StringBuffer subject = new StringBuffer();
		StringBuffer body = new StringBuffer();
		String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
		List list = null;// new ArrayList();
		String mailSubject = null;
		RowMapper getArticleOwnerRowMapper = new RowMapper() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String eMail = rs.getString("REALEMAIL");
				return eMail;
			}
		};
		list = getJdbcTemplate().query(DRBQueryConstants.GET_EMAIL_ID,
				new Object[] { ownerSSOId }, getArticleOwnerRowMapper);
		if (list != null && list.size() > 0) {
			if (list.get(0) != null) {
				String ownerMailId = list.get(0).toString();
				toMail.add(ownerMailId);
			}
		}
		body.append(ArticleConstants.HTML_TAG);
		body.append(ArticleConstants.HTML_BODY_TAG);
		body.append(ArticleConstants.HTML_TABLE_TAG);
		body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
		body.append(ArticleConstants.ARTICLE_NO_LABLE + " ");
		/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
		body.append(ArticleConstants.AHREF).append(linkUrl)
				.append("?articleSeqID=").append(articleSeqId);
		/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
		body.append(" target=\"new\">");
		body.append(articleNumber);
		body.append("</a>");
		if (userRole != null
				&& ArticleConstants.EDRB_ROLE_03.equalsIgnoreCase(userRole)) {
			mailSubject = ArticleConstants.OWNER_EMAIL_SUBJECT_FOR_REASSIGN;
			body.append(ArticleConstants.EMPTY_WITH_SPACE
					+ ArticleConstants.OWNER_EMAIL_BODY_01 + " " + userName);
			body.append(ArticleConstants.EDRB_DOT_STRING);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.OWNER_EMAIL_BODY_02 + " "
					+ newUserName);

		} else if (userRole != null
				&& ArticleConstants.EDRB_ROLE_04.equalsIgnoreCase(userRole)) {
			mailSubject = ArticleConstants.OWNER_EMAIL_SUBJECT_FOR_ADD_REVIEWER;
			body.append(ArticleConstants.EMPTY_WITH_SPACE
					+ ArticleConstants.OWNER_EMAIL_BODY_03 + " " + userName
					+ " ");
			body.append(ArticleConstants.OWNER_EMAIL_BODY_04 + " "
					+ newUserName);
		}
		body.append(ArticleConstants.EDRB_DOT_STRING);
		body.append(ArticleConstants.TABLE_ROW_END_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.AUTO_GENERATED);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.HTML_TABLE_END_TAG);
		body.append(ArticleConstants.HTML_BODY_END_TAG);
		body.append(ArticleConstants.HTML_END_TAG);
		subject.append(mailSubject);
		_mailBean.sendMail(toMail, ccMail, subject.toString(), body.toString(),
				_mailSend, ArticleConstants.TEST);
		body = null;
	}

	/**
	 * @param userId
	 * @param articleNumber
	 * @throws DRBCommonException
	 */
	public void sendArticleOwnerChangeMail(String ownerEmail,
			String articleNumber, String articleSeqId)
			throws DRBCommonException {
		List<String> toMail = new ArrayList<String>();
		List<String> ccMail = new ArrayList<String>();
		StringBuffer subject = new StringBuffer();
		StringBuffer body = new StringBuffer();
		String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
		toMail.add(ownerEmail);
		subject.append(ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_SUBJECT_OWNER);
		body.append(ArticleConstants.HTML_TAG);
		body.append(ArticleConstants.HTML_BODY_TAG);
		body.append(ArticleConstants.HTML_TABLE_TAG);
		body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_BODY1);
		body.append(ArticleConstants.ARTICLE_NO_LABLE);
		/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
		body.append(ArticleConstants.AHREF).append(linkUrl)
				.append("?articleSeqID=").append(articleSeqId);
		/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
		body.append(" target=\"new\">");
		body.append(articleNumber);
		body.append("</a>");
		body.append(" " + ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_BODY2);
		body.append(ArticleConstants.EDRB_DOT_STRING);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.AUTO_GENERATED);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.TABLE_ROW_END_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.HTML_TABLE_END_TAG);
		body.append(ArticleConstants.HTML_BODY_END_TAG);
		body.append(ArticleConstants.HTML_END_TAG);
		_mailBean.sendMail(toMail, ccMail, subject.toString(), body.toString(),
				_mailSend, ArticleConstants.TEST);
		body = null;// heap issue
	}

	/**
	 * This method will update the Description of the Attachments edited by the
	 * user, in the Database
	 * 
	 * @param attachmentNo
	 *            , description
	 */
	@Transactional
	public String saveDescription(final String articleNo,
			final String[] attachmentNo, final String[] description)
			throws DRBCommonException {
		String str = null;
		final int count = description.length;
		UserData userObj = (UserData) (DRBUtils.getServletSession(true)
				.getAttribute("USER_DATA"));
		final String lastUpdatedBy = DRBUtils.setPersonnelFullName(
				userObj.getUserLastName(), userObj.getUserFirstName());
		// Log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> in DAO  saving attachement description");
		int[] rowsUpdatedByEachStatement = getJdbcTemplate().batchUpdate(
				DRBQueryConstants.DOCUMENT_DESC_SAVE_QUERY,
				new BatchPreparedStatementSetter() {
					public void setValues(PreparedStatement ps, int nextValue)
							throws SQLException {
						ps.setString(1, description[nextValue]);
						ps.setString(2, lastUpdatedBy);
						ps.setString(3, attachmentNo[nextValue]);
						ps.setString(4, articleNo);
					}

					public int getBatchSize() {
						return count;
					}

				});

		if (rowsUpdatedByEachStatement != null
				&& rowsUpdatedByEachStatement.length == description.length) {
			str = ArticleConstants.STATE_SUCCESS;
		} else {
			str = ArticleConstants.STATE_FAILURE;
		}
		return str;
	}

	/**
	 * @param requestEventSeqId
	 * @return String
	 */
	public String getArticleData(String requestEventSeqId) {
		String REVIEW_ARTICLE_DATA = "SELECT APP_DATA_PK_ID,REQUEST_TYPE_NAME FROM CAWC.DRB_REQUEST_EVENT RE,CAWC.DRB_REQUEST_TYPE RT "
				+ " WHERE RE.REQUEST_TYPE_ID=RT.REQUEST_TYPE_ID AND REQUEST_EVENT_SEQ_ID=?";
		List<String> articleData = getSimpleJdbcTemplate().query(
				REVIEW_ARTICLE_DATA, new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						String edited = rs.getString("APP_DATA_PK_ID") + "~"
								+ rs.getString("REQUEST_TYPE_NAME");
						;
						return edited;
					}
				}, requestEventSeqId);
		return articleData.get(0);
	}

	/**
	 * @param articleSeqId
	 * @return String
	 * @throws DRBCommonException
	 */
	public List<UserData> notifyArticleList(String articleSeqId)
			throws DRBCommonException {

		articleSeqId = articleSeqId == null ? "0" : articleSeqId;

		List<UserData> notifyList = getSimpleJdbcTemplate().query(
				DRBQueryConstants.GET_NOTIFY_USERS,
				new ParameterizedRowMapper<UserData>() {
					public UserData mapRow(ResultSet rs, int arg1)
							throws SQLException {
						UserData userData = new UserData();
						userData.setUserSSOID(rs.getString(1));
						userData.setUserEMailID(rs.getString(2));
						userData.setUserFirstName(rs.getString(3));
						userData.setUserLastName(rs.getString(4));
						return userData;
					}
				}, Integer.parseInt(articleSeqId));
		// Log.info("the list sizeeeeeeeee " + notifyList.size());
		return notifyList;
	}

	/**
	 * @param articleNo
	 * @param targetFolderSeqId
	 * @param userId
	 * @return String
	 * @throws DRBCommonException
	 */
	public String moveArticle(String srcArticleSeqId, String targetFolderSeqId,
			String userId, String userName) throws DRBCommonException {
		String retVal = null;
		// Log.info("moveArticle() IMPL :: articleNo :: " + srcArticleSeqId);
		// Log.info("moveArticle() IMPL :: targetFolderSeqId :: " +
		// targetFolderSeqId);
		// Log.info("moveArticle() IMPL :: userId :: " + userId);
		// Log.info("moveArticle() IMPL :: userName :: " + userName);
		_moveArticlePrc.excuteMoveArticleProc(srcArticleSeqId,
				targetFolderSeqId, userName, userId);
		return retVal;
	}

	/**
	 * @param articleSeqId
	 * @param articleNo
	 * @param reason
	 * @param userID
	 * @throws DRBCommonException
	 */
	public void sendArticleTaskMail(ArticleData articleData, String artTitle,
			String articleSeqId, String articleNo, String reason,
			String revComments, String addReviCommenst, String metaDataField,
			String... userID) throws DRBCommonException {
		try {
			List<String> mailList = new ArrayList<String>();
			List<String> toMail = new ArrayList<String>();
			List<String> ccMail = new ArrayList<String>();
			String sexportStatus = articleData.getArticleEcTag();
			String reviewerName = null;
			String addReviewerName = null;
			String articleRevDis = null;
			String articleAddRevDis = null;
			/**
			 * Added by Pradeep for Enhancement No: 64
			 */
			if (!reason.equalsIgnoreCase("rejected")) {
				List<UserData> notifylist = notifyArticleList(articleSeqId);
				for (UserData tonote : notifylist) {
					ccMail.add(tonote.getUserEMailID());
				}
			}

			StringBuffer subject = null;
			StringBuffer body = null;
			subject = new StringBuffer();
			body = new StringBuffer();
			for (String id : userID) {
				mailList.add(id);
			}

			if (userID[0].equalsIgnoreCase(userID[1])) {
				mailList.remove(userID[1]);
			}
			if (userID[0].equalsIgnoreCase(userID[2])) {
				mailList.add(userID[2]);
			}
			UserData userObj = (UserData) (DRBUtils.getServletSession(false)
					.getAttribute("USER_DATA"));
			String loginID = userObj.getUserSSOID();
			String userfirstName = userObj.getUserFirstName();
			String userlastName = userObj.getUserLastName();
			List<UserData> userEmail = _userServiceObj.getUserDetails(mailList);
			for (UserData email : userEmail) {
				if (email.getUserSSOID().equalsIgnoreCase(loginID)) {
					;
				} else {
					if (reason.equalsIgnoreCase("edited")
							&& (email.getUserSSOID().equals(userID[1]) || email
									.getUserSSOID().equals(userID[2]))) {
						;
					} else {
						toMail.add(email.getUserEMailID());
					}
				}
				// Added by Vasantha for getting both reviewers mail id of
				// reject article
				if (reason.equalsIgnoreCase("rejected")
						&& (email.getUserSSOID().equalsIgnoreCase(loginID))) {
					toMail.add(email.getUserEMailID());
				}// end

				if (loginID.equalsIgnoreCase(userID[0])
						&& userID[0].equalsIgnoreCase(userID[1])
						&& email.getUserSSOID().equals(userID[1])) {
					toMail.add(email.getUserEMailID());
				} else if (loginID.equalsIgnoreCase(userID[0])
						&& userID[0].equalsIgnoreCase(userID[2])
						&& email.getUserSSOID().equals(userID[2])) {
					toMail.add(email.getUserEMailID());
				}
				if (email.getUserSSOID().equals(userID[0])) {
					DRBUtils.setEmailPersonnelFullName(email.getUserLastName(),
							email.getUserFirstName());
				} else if (email.getUserSSOID().equals(userID[1])) {
					reviewerName = DRBUtils.setEmailPersonnelFullName(
							email.getUserLastName(), email.getUserFirstName());
				} else if (email.getUserSSOID().equals(userID[2])) {
					addReviewerName = DRBUtils.setEmailPersonnelFullName(
							email.getUserLastName(), email.getUserFirstName());
				}
			}

			/*******************************************************************************************
			 * updated method
			 */
			String ARTICLE_STATUS = DRBQueryConstants.REVIEWERS_DISPOSITOINS;
			List<String> articleStatus = getSimpleJdbcTemplate().query(
					ARTICLE_STATUS, new ParameterizedRowMapper<String>() {
						public String mapRow(ResultSet rs, int arg1)
								throws SQLException {
							String articleRevDis = "NO";
							String articleAddRevDis = "NO";
							if (rs.getString("REVIEWER_USER_ID") != null
									&& !"".equals(rs
											.getString("REVIEWER_USER_ID"))) {
								if (rs.getString("REVIEW_DISPOSITION") != null
										&& rs.getString("REVIEW_DISPOSITION")
												.equalsIgnoreCase("APPROVED")) {
									articleRevDis = "approved";
								} else if (rs.getString("REVIEW_DISPOSITION")
										.equalsIgnoreCase("REJECTED")) {
									articleRevDis = "rejected";
								}
							}
							// Log.info("Reviwer>>>>>>>>>>>>>>>>>Status" +
							// articleRevDis);
							if (rs.getString("ADDITIONAL_REVIEWER_USER_ID") != null
									&& !"".equals(rs
											.getString("ADDITIONAL_REVIEWER_USER_ID"))) {
								if (rs.getString("ADDITIONAL_REVIEW_DISPOSITION") != null
										&& rs.getString(
												"ADDITIONAL_REVIEW_DISPOSITION")
												.equalsIgnoreCase("APPROVED")) {
									articleAddRevDis = "approved";
								} else if (rs.getString(
										"ADDITIONAL_REVIEW_DISPOSITION")
										.equalsIgnoreCase("REJECTED")) {
									articleAddRevDis = "rejected";
								}
							}
							// Log.info("ADD Reviwer>>>>>>>>>>>>>>>>>Status" +
							// articleAddRevDis);
							return articleRevDis + "~" + articleAddRevDis;
						}
					}, articleSeqId);
			String totalStatus = "";
			if (articleStatus != null) {
				totalStatus = articleStatus.get(0);
			}
			// Log.info("total>>>>>>>>>>>>>>>>>Status" + totalStatus);
			if (totalStatus != null) {
				articleRevDis = totalStatus.split("~")[0];
				articleAddRevDis = totalStatus.split("~")[1];
			}
			// Log.info(">>>>>>>>>>>>>>>>>Status" + articleRevDis +
			// ">>>>>>>>>>>>>" + articleAddRevDis);
			if (reason.equalsIgnoreCase("owner")) {
				// Log.info("i am here befoe sending sendArticleTaskMail" );
				subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTOWNERAPP);
				subject.append(articleNo);
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					subject.append(ArticleConstants.DRB_DASH);
					subject.append(artTitle);
				}
				body.append(ArticleConstants.HTML_TAG);
				body.append(ArticleConstants.HTML_BODY_TAG);
				body.append(ArticleConstants.HTML_TABLE_TAG);
				body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
				body.append(userfirstName);
				body.append(" ");
				body.append(userlastName);
				body.append(" has approved the changes made in the article properties by the reviewer(s) for DRB article ");
				// body.append(ArticleConstants.SPE_SYMBOL);
				String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(articleSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(articleNo);
				body.append("</a>");
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					body.append(ArticleConstants.DRB_DASH);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
					body.append(ArticleConstants.AHREF).append(linkUrl)
							.append("?articleSeqID=").append(articleSeqId);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
					body.append(" target=\"new\">");
					body.append(artTitle);
					body.append("</a>");
				}
				body.append(":");
				body.append(" The article has now issued.");
			} else if (reason.equalsIgnoreCase("ownerreject")) {
				// Log.info("i am here befoe sending sendArticleTaskMail  ownerreject"
				// );
				subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTOWNERREJ);
				subject.append(articleNo);
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					subject.append(ArticleConstants.DRB_DASH);
					subject.append(artTitle);
				}
				body.append(ArticleConstants.HTML_TAG);
				body.append(ArticleConstants.HTML_BODY_TAG);
				body.append(ArticleConstants.HTML_TABLE_TAG);
				body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
				body.append(userfirstName);
				body.append(" ");
				body.append(userlastName);
				body.append(" has rejected the changes made in the article properties by the reviewer(s) for DRB article ");
				String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(articleSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(articleNo);
				body.append("</a>");
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					body.append(ArticleConstants.DRB_DASH);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
					body.append(ArticleConstants.AHREF).append(linkUrl)
							.append("?articleSeqID=").append(articleSeqId);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
					body.append(" target=\"new\">");
					body.append(artTitle);
					body.append("</a>");
				}
				body.append(":");
				body.append(" The article has returned to the Draft state for further work and review.");

			} else if (reason.equalsIgnoreCase("approve")) {
				subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTAPPROVED);
				subject.append(articleNo);
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					subject.append(ArticleConstants.DRB_DASH);
					subject.append(artTitle);
				}
				body.append(ArticleConstants.HTML_TAG);
				body.append(ArticleConstants.HTML_BODY_TAG);
				body.append(ArticleConstants.HTML_TABLE_TAG);
				body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
				body.append(ArticleConstants.REVIWER_BODY_PART0NE);
				String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(articleSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(articleNo);
				body.append("</a>");
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					body.append(ArticleConstants.DRB_DASH);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
					body.append(ArticleConstants.AHREF).append(linkUrl)
							.append("?articleSeqID=").append(articleSeqId);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
					body.append(" target=\"new\">");
					body.append(artTitle);
					body.append("</a>");
				}
				body.append(" has been approved for issue following review by ");
				body.append(reviewerName);
				/* Venkat updated on 30/12/2010 for compare strings as per SQDB */
				if (articleData.getAdditionalReviewer() != null
						&& !(ArticleConstants.EMPTY
								.equalsIgnoreCase(articleData
										.getAdditionalReviewer()))) {
					body.append(" and by ");
					body.append(addReviewerName);
				}
				body.append(ArticleConstants.EDRB_DOT_STRING);
			} else if (reason
					.equalsIgnoreCase(ArticleConstants.STATE_ISSUEDWITHEDITS)) {
				subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTAPPROVED);
				subject.append(articleNo);
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					subject.append(ArticleConstants.DRB_DASH);
					subject.append(artTitle);
				}
				body.append(ArticleConstants.HTML_TAG);
				body.append(ArticleConstants.HTML_BODY_TAG);
				body.append(ArticleConstants.HTML_TABLE_TAG);
				body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
				body.append(ArticleConstants.REVIWER_BODY_PART0NE);
				String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(articleSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(articleNo);
				body.append("</a>");
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					body.append(ArticleConstants.DRB_DASH);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
					body.append(ArticleConstants.AHREF).append(linkUrl)
							.append("?articleSeqID=").append(articleSeqId);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
					body.append(" target=\"new\">");
					body.append(artTitle);
					body.append("</a>");
				}
				body.append(" has been approved for issue by following review");
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				if (revComments != null) {
					body.append("Comments by the Primary reviewer, ");
					body.append(reviewerName);
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					body.append(revComments);
				} else {
					body.append(" by ");
					body.append(reviewerName);
				}
				/* Venkat updated on 30/12/2010 for compare strings as per SQDB */
				if (articleData.getAdditionalReviewer() != null
						&& !(ArticleConstants.EMPTY
								.equalsIgnoreCase(articleData
										.getAdditionalReviewer()))) {
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					if (addReviCommenst != null) {
						body.append("And comments by the additional reviewer, ");
						body.append(addReviewerName);
						body.append(ArticleConstants.LINE_BRAKE_TAG);
						body.append(addReviCommenst);
					} else {
						body.append(" and by ");
						body.append(addReviewerName);
					}
				}
				body.append(ArticleConstants.EDRB_DOT_STRING);
			} else if (reason
					.equalsIgnoreCase(ArticleConstants.STATE_APPROVEDWITHEDITS)) {
				subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTAPPROVEDS_S);
				subject.append(articleNo);
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					subject.append(ArticleConstants.DRB_DASH);
					subject.append(artTitle);
				}
				body.append(ArticleConstants.HTML_TAG);
				body.append(ArticleConstants.HTML_BODY_TAG);
				body.append(ArticleConstants.HTML_TABLE_TAG);
				body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
				body.append(ArticleConstants.REVIWER_BODY_PART0NE);
				String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(articleSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(articleNo);
				body.append("</a>");
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					body.append(ArticleConstants.DRB_DASH);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
					body.append(ArticleConstants.AHREF).append(linkUrl)
							.append("?articleSeqID=").append(articleSeqId);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
					body.append(" target=\"new\">");
					body.append(artTitle);
					body.append("</a>");
				}
				body.append(" has been approved with edits by following review");
				if (loginID.equalsIgnoreCase(articleData.getReviewerid())) {
					if (articleData.getReviewerComments() != null) {
						body.append(ArticleConstants.LINE_BRAKE_TAG);
						body.append("Comments by the Primary reviewer, ");
						body.append(reviewerName);
						body.append(ArticleConstants.LINE_BRAKE_TAG);
						body.append(articleData.getReviewerComments());
						body.append(ArticleConstants.LINE_BRAKE_TAG);
					}
				} else if (loginID.equalsIgnoreCase(articleData
						.getAdditionalReviewerId())) {
					/*
					 * Venkat updated on 30/12/2010 for compare strings as per
					 * SQDB
					 */
					if (articleData.getAdditionalReviewer() != null
							&& !(ArticleConstants.EMPTY
									.equalsIgnoreCase(articleData
											.getAdditionalReviewer()))) {
						if (articleData.getAdditionalReviewerComments() != null) {
							body.append(ArticleConstants.LINE_BRAKE_TAG);
							body.append("Comments by the additional reviewer, ");
							body.append(addReviewerName);
							body.append(ArticleConstants.LINE_BRAKE_TAG);
							body.append(articleData
									.getAdditionalReviewerComments());
						}
					}
				}
				body.append(ArticleConstants.EDRB_DOT_STRING);
			} else if (reason.equalsIgnoreCase("edited")) {
				subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTEDITEDAPPROVED);
				body.append(ArticleConstants.HTML_TAG);
				body.append(ArticleConstants.HTML_BODY_TAG);
				body.append(ArticleConstants.HTML_TABLE_TAG);
				body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
				body.append(ArticleConstants.REVIWER_BODY_PART0NE);
				body.append(ArticleConstants.SPE_SYMBOL);
				String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
				/*
				 * body.append("<a href=").append(linkUrl).append("?articleSeqID="
				 * ).append (articleSeqId); body.append(" target=\"new\">");
				 */
				body.append(articleNo);
				/* body.append("</a>  "); */
				body.append(ArticleConstants.REVIWER_BODY_PARTTWO_EDITED);
				if ("Y".equalsIgnoreCase(metaDataField)) {
					body.append(reviewerName + " & " + addReviewerName);
				} else if ("P".equalsIgnoreCase(metaDataField)) {
					body.append(reviewerName);
				} else {
					body.append(addReviewerName);
				}
				body.append(ArticleConstants.EDRB_DOT_STRING);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append("The changed metadata fields are as follows : ");
				List<String> chagedData = saveModifiedData(articleSeqId);
				StringBuffer dataToSave = new StringBuffer("");
				if (chagedData != null && chagedData.size() > 0) {
					for (String tempdata : chagedData) {
						dataToSave.append(tempdata).append(", ");
					}
				}
				StringBuffer tempdata = new StringBuffer(dataToSave.substring(
						0, dataToSave.length() - 2));
				if (tempdata.lastIndexOf(",") > 0) {
					tempdata.replace(tempdata.lastIndexOf(","),
							tempdata.lastIndexOf(",") + 1, " and");
				}
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(tempdata.toString());
				body.append(ArticleConstants.EDRB_DOT_STRING);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(ArticleConstants.REVIWER_BODY_PARTTHIRD_EDITED);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append("Please click on the below link to navigate to My Tasks.");
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?emailMyTask=MYTASK");
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append("My tasks");
				body.append("</a>");
				ccMail.clear(); // clearing notification list only to owner and
				// reviewers
			} else if (reason.equalsIgnoreCase("rejected")) {
				subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTOWNERREJ);
				subject.append(articleNo);
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					subject.append(ArticleConstants.DRB_DASH);
					subject.append(artTitle);
				}
				body.append(ArticleConstants.HTML_TAG);
				body.append(ArticleConstants.HTML_BODY_TAG);
				body.append(ArticleConstants.HTML_TABLE_TAG);
				body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
				body.append(ArticleConstants.REVIWER_BODY_PART0NE);
				String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(articleSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(articleNo);
				body.append("</a>");
				if (sexportStatus != null
						&& (sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
								.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
					body.append(ArticleConstants.DRB_DASH);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
					body.append(ArticleConstants.AHREF).append(linkUrl)
							.append("?articleSeqID=").append(articleSeqId);
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
					body.append(" target=\"new\">");
					body.append(artTitle);
					body.append("</a>");
				}

				body.append(" has been rejected following review.");
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append("Comments by the Primary reviewer, ");
				body.append(reviewerName);
				body.append(":");
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				// Start: Added by vasantha for showing the Primary Reviewer
				// Comments is empty
				if (articleRevDis != null) {
					if (articleData.getReviewerComments() != null
							&& articleRevDis
									.equalsIgnoreCase(ArticleConstants.REJECTED)) {
						body.append(ArticleConstants.LINE_BRAKE_TAG);
						body.append(articleData.getReviewerComments());
					}
				}
				// end: Added by vasantha for showing the Primary Reviewer
				// Comments is empty
				if (articleData.getAdditionalReviewer() != null
						&& !articleData.getAdditionalReviewer()
								.equalsIgnoreCase("")) {
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					body.append("Comments by the additional reviewer, ");
					body.append(addReviewerName);
					body.append(":");
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					// Start: Added by vasantha for showing the Additional
					// Reviewer Comments is empty
					if (articleAddRevDis != null) {
						if (articleData.getAdditionalReviewerComments() != null
								&& articleAddRevDis
										.equalsIgnoreCase(ArticleConstants.REJECTED)) {
							body.append(ArticleConstants.LINE_BRAKE_TAG);
							body.append(articleData
									.getAdditionalReviewerComments());
						}
					}
					// end: Added by vasantha for showing the Additional
					// Reviewer Comments is empty
				}
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append(ArticleConstants.LINE_BRAKE_TAG);
				body.append("Please review these comments, consult with the reviewer(s) and resubmit the article for approval.");
			}
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.AUTO_GENERATED);
			body.append(ArticleConstants.TABLE_ROW_END_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.HTML_TABLE_END_TAG);
			body.append(ArticleConstants.HTML_BODY_END_TAG);
			body.append(ArticleConstants.HTML_END_TAG);
			_mailBean.sendMail(toMail, ccMail, subject.toString(),
					body.toString(), _mailSend, ArticleConstants.TEST);
			// Log.info("i am here after sending sendArticleTaskMail  ownerreject"
			// );
			body = null;// heap issue
		} catch (MailSendException e) {// Code Review Changed By Naresh on
										// 22DEC10
			LOG.error("Exception[sendArticleTaskMail]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);
		} catch (Exception e) {
			LOG.error("Exception[sendArticleTaskMail]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);
		}

	}

	/**
	 * @param articleSeqId
	 * @param articleNo
	 * @param reason
	 * @param ownerId
	 * @param reviewerId
	 * @param name
	 * @throws DRBCommonException
	 */
	public void sendArticleToReviewer(String articleSeqId, String articleNo,
			String reason, String ownerId, String reviewerId, String name,
			String otherUserId, String status) throws DRBCommonException {
		ArrayList<String> mailList = new ArrayList<String>();
		ArrayList<String> toMail = new ArrayList<String>();
		ArrayList<String> ccMail = new ArrayList<String>();
		StringBuffer subject = null;
		StringBuffer body = null;
		subject = new StringBuffer();
		body = new StringBuffer();
		mailList.add(ownerId);
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute("USER_DATA"));
		String loginID = userObj.getUserSSOID();
		List<UserData> userEmail = _userServiceObj.getUserDetails(mailList);
		for (UserData email : userEmail) {
			if (email.getUserSSOID().equalsIgnoreCase(loginID)) {
				;
			} else {
				toMail.add(email.getUserEMailID());
			}
		}
		if ("approved".equalsIgnoreCase(status)) {
			subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTAPPROVEDS_S);
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append(ArticleConstants.REVIWER_BODY_PART0NE);
			body.append(ArticleConstants.SPE_SYMBOL);
			body.append(articleNo);
			body.append(ArticleConstants.EDITED_BY_REVIEWERONE);
			body.append(name);
		} else if ("editedapproved".equalsIgnoreCase(status)) {
			subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTAPPROVED_S);
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append(ArticleConstants.REVIWER_BODY_PART0NE);
			body.append(ArticleConstants.SPE_SYMBOL);
			body.append(articleNo);
			body.append(ArticleConstants.EDITED_BY_REVIEWERONE);
			body.append(name);
			body.append(ArticleConstants.EDITED_BY_REVIEWERTWO);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.REJECTION_COMMENTS_METACHANGESRTACC
					+ " : " + reason);
		} else if ("rejected".equalsIgnoreCase(status)) {
			subject.append(ArticleConstants.REVIEWER_ARTICLE_SUBJECTREJECTED_S);
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append(ArticleConstants.REVIWER_BODY_PART0NE);
			body.append(ArticleConstants.SPE_SYMBOL);
			body.append(articleNo);
			body.append(ArticleConstants.EDITED_BY_REVIEWERREJECT);
			body.append(name);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.REJECTION_COMMENTS_REJARTACC + reason);
		}
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.AUTO_GENERATED);
		body.append(ArticleConstants.TABLE_ROW_END_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.HTML_TABLE_END_TAG);
		body.append(ArticleConstants.HTML_BODY_END_TAG);
		body.append(ArticleConstants.HTML_END_TAG);
		_mailBean.sendMail(toMail, ccMail, subject.toString(), body.toString(),
				_mailSend, ArticleConstants.TEST);
		body = null;// heap issue
	}

	/**
	 * @param articleNo
	 * @param toMailList
	 * @param articleState
	 */
	public void stateChangeEmail(String articleNo, List<String> toMailList,
			String articleState, ArticleData articleData) {
		try {
			String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
			List<String> toMail = toMailList;
			ArrayList<String> ccMail = new ArrayList<String>();
			StringBuffer subject = null;
			StringBuffer body = null;
			subject = new StringBuffer();
			String sexportStatus = articleData.getArticleEcTag();
			body = new StringBuffer();
			subject.append(ArticleConstants.ARTICLE_STATE_CHANGE_EMAIL_SUBJECT);
			subject.append(articleNo);
			if (sexportStatus != null
					&& (sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
				subject.append(ArticleConstants.DRB_DASH);
				subject.append(articleData.getArticleTitle());
			}
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append(ArticleConstants.ARTICLE_NO_LABLE);
			body.append(articleNo);
			body.append(ArticleConstants.ARTICLE_NUMBERED);
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
			body.append(ArticleConstants.AHREF).append(linkUrl)
					.append("?articleSeqID=")
					/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */

					.append(articleData.getArticleSeqid());

			body.append(" target=\"new\">");

			body.append(articleData.getArticleTitle());

			body.append("</a>");
			body.append(ArticleConstants.ARTICLE_STATE_CHANGED_FROM_DRAFT_TO);
			body.append("'");
			body.append(articleState);
			body.append("'");
			body.append(ArticleConstants.TABLE_ROW_END_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.AUTO_GENERATED);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.HTML_TABLE_END_TAG);
			body.append(ArticleConstants.HTML_BODY_END_TAG);
			body.append(ArticleConstants.HTML_END_TAG);
			_mailBean.sendMail(toMail, ccMail, subject.toString(),
					body.toString(), _mailSend, ArticleConstants.TEST);
			body = null;// heap issue
		} catch (MailSendException e) {// Code Review Change By Naresh on
										// 22DEC10
			LOG.error("Exception[MailSendException]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception[stateChangeEmail]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);
		}
	}

	/**
	 * @param articleNo
	 * @param reviewer
	 */
	public void emailToReviewer(String articleNo, String reviewer,
			String artTitle, String nlrOrNot, String artSeqId) {
		// Log.info("i am here befoe sending mailemailToReviewer" +nlrOrNot);
		// Log.info("i am here befoe sending mailemailToReviewer" +artSeqId);
		try {
			String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
			ArrayList<String> toMail = new ArrayList<String>();
			toMail.add(reviewer);

			String currDate = null;
			String REVIEW_DATE = DRBQueryConstants.REVIEW_DATE_ARTICLE;
			List<String> isfolderID = getSimpleJdbcTemplate().query(
					REVIEW_DATE, new ParameterizedRowMapper<String>() {
						public String mapRow(ResultSet rs, int arg1)
								throws SQLException {
							String edited = rs.getString("DATE_VALUE");
							return edited;
						}
					}, artSeqId);
			currDate = isfolderID.get(0);
			ArrayList<String> ccMail = new ArrayList<String>();
			StringBuffer subject = null;
			StringBuffer body = null;
			subject = new StringBuffer();
			UserData userObj = (UserData) (DRBUtils.getServletSession(false)
					.getAttribute("USER_DATA"));
			String userfirstName = userObj.getUserFirstName();
			String userlastName = userObj.getUserLastName();
			body = new StringBuffer();
			subject.append("Please Review DRB Article: ");
			subject.append(articleNo);
			if (nlrOrNot != null
					&& (nlrOrNot.equalsIgnoreCase(ArticleConstants.DRB_NLR) || nlrOrNot
							.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
				subject.append(ArticleConstants.DRB_DASH);
				subject.append(artTitle);
			}
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append("You have been selected to review DRB article ");
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
			body.append(ArticleConstants.AHREF).append(linkUrl)
					.append("?articleSeqID=").append(artSeqId);
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
			body.append(" target=\"new\">");
			body.append(articleNo);
			body.append("</a>");
			if (nlrOrNot != null
					&& (nlrOrNot.equalsIgnoreCase(ArticleConstants.DRB_NLR) || nlrOrNot
							.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
				body.append(ArticleConstants.DRB_DASH);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
				body.append(ArticleConstants.AHREF).append(linkUrl)
						.append("?articleSeqID=").append(artSeqId);
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
				body.append(" target=\"new\">");
				body.append(artTitle);
				body.append("</a>");
			}
			body.append(" by ");
			body.append(userfirstName);
			body.append(" ");
			body.append(userlastName);
			body.append(ArticleConstants.TABLE_ROW_END_TAG);

			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append("Please complete your review by ");
			body.append(currDate);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append("This task has been added to your DRB ");
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
			body.append(ArticleConstants.AHREF).append(linkUrl)
					.append("?emailMyTask=MYTASK");
			/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */
			body.append(" target=\"new\">");
			body.append("Tasks");
			body.append("</a>");
			body.append(".");
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.AUTO_GENERATED);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.HTML_TABLE_END_TAG);
			body.append(ArticleConstants.HTML_BODY_END_TAG);
			body.append(ArticleConstants.HTML_END_TAG);
			_mailBean.sendMail(toMail, ccMail, subject.toString(),
					body.toString(), _mailSend, ArticleConstants.TEST);
			// Log.info("i am here after sending mailemailToReviewer" );
			body = null;// heap issue
		} catch (MailSendException e) {// Code Review Change By Naresh on
										// 22DEC10
			LOG.error("Exception[MailSendException]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception[emailToReviewer]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		}
	}

	/**
	 * @param articleNo
	 * @param toMailList
	 * @param articleState
	 */
	public void versionChangeEmail(String articleNo, List<String> toMailList,
			String articleState, ArticleData articleData) {
		String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
		List<String> toMail = toMailList;
		ArrayList<String> ccMail = new ArrayList<String>();
		StringBuffer subject = null;
		StringBuffer body = null;
		subject = new StringBuffer();
		body = new StringBuffer();
		subject.append(ArticleConstants.ARTICLE_VERSION_CHANGE_EMAIL_SUBJECT);
		body.append(ArticleConstants.HTML_TAG);
		body.append(ArticleConstants.HTML_BODY_TAG);
		body.append(ArticleConstants.HTML_TABLE_TAG);
		body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
		body.append(ArticleConstants.ARTICLE_NO_LABLE);
		body.append(articleData.getArticleNumber());
		body.append(ArticleConstants.ARTICLE_NUMBERED);
		/* Defect Id:1211 Subhrajyoti 22-DEc-2010 Start */
		body.append(ArticleConstants.AHREF).append(linkUrl)
				.append("?articleSeqID=")
				/* Defect Id:1211 Subhrajyoti 22-DEc-2010 End */

				.append(articleNo);

		body.append(" target=\"new\">");

		body.append(articleData.getArticleTitle());

		body.append("</a>");
		body.append(ArticleConstants.ARTICLE_STATE_TO_CHECKEDOUT);
		body.append(ArticleConstants.TABLE_ROW_END_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.AUTO_GENERATED);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.HTML_TABLE_END_TAG);
		body.append(ArticleConstants.HTML_BODY_END_TAG);
		body.append(ArticleConstants.HTML_END_TAG);
		_mailBean.sendMail(toMail, ccMail, subject.toString(), body.toString(),
				_mailSend, ArticleConstants.TEST);
		body = null;// heap issue
	}

	/*********************** Start of Methods Added by Aishwarya for Audit Folder *************************/

	/**
	 * @purpose This method is used to update the Audit Status based on the age
	 *          of the lastly issued Article
	 * @param folderSeqID
	 * @param articleSeqID
	 * @param updatedBy
	 * @throws DRBCommonException
	 */
	@Transactional
	private void auditStatusUpdationOnArticleIssue(String folderSeqID,
			String articleSeqID, String updatedBy) throws DRBCommonException {
		String isArtOld = null;
		String oldArtCount = null;
		// int iCount = 0;
		// Log.info("Is the pfolderSeqID???????????? " + folderSeqID);
		// Log.info("Is the articleSeqID???????????? " + articleSeqID);
		// Log.info("Is the updatedBy???????????? " + updatedBy);

		isArtOld = getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.IS_ARTICLE_OLD, oldArticleCountMapper,
				new Object[] { DRBUtils.checkNullVal(articleSeqID) });
		// Log.info("Is the promoted Article old???????????? " + isArtOld);
		if (!isArtOld.equalsIgnoreCase(FolderConstants.STR_ZERO)) {
			oldArtCount = getSimpleJdbcTemplate().queryForObject(
					DRBQueryConstants.IS_LAST_UNISSUED_ARTICLE,
					oldArticleCountMapper,
					new Object[] { DRBUtils.checkNullVal(folderSeqID) });
			// Log.info("Number of old Articles in the folder!!!!!!!!!!! " +
			// oldArtCount);
			if (oldArtCount.equalsIgnoreCase(FolderConstants.STR_ZERO)) {
				/* iCount = */
				getJdbcTemplate().update(
						DRBQueryConstants.UPD_AUDIT_STS_ART_ISSUE,
						new Object[] { DRBUtils.checkNullVal(updatedBy),
								DRBUtils.checkNullVal(folderSeqID) });
				// Log.info("Audit Status is updated###### " + iCount);
			}
		}
	}

	/*********************** End of Methods Added by Aishwarya for Audit Folder *************************/
	/********************* add by Raju ****************************/

	// saurabh methods atarts here
	/**
	 * @param librarySeqId
	 * @param articleSeqId
	 * @return List<ArticleData>
	 * @throws DRBCommonException
	 */
	public List<ArticleData> getCheckListDetails(String librarySeqId,
			String articleSeqId) throws DRBCommonException {
		List<ArticleData> checkListDetails = null;
		StringBuffer query = null;
		query = new StringBuffer();
		query.append(DRBQueryConstants.FETCH_ARTICLE_CHECKLIST_QUERY_02);
		checkListDetails = getSimpleJdbcTemplate().query(query.toString(),
				new ParameterizedRowMapper<ArticleData>() {
					public ArticleData mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ArticleData articleData = new ArticleData();
						articleData.setChecklistQuesSeqId(rs
								.getInt("CHECKLIST_QUESTION_SEQ_ID"));
						articleData.setQuesorder(rowNum + 1);
						articleData.setQuestions(rs.getString("QUESTION_TEXT"));
						return articleData;
					}
				}, Integer.parseInt(librarySeqId));
		StringBuffer query1 = null;
		query1 = new StringBuffer();
		query1.append(DRBQueryConstants.FETCH_ARTICLE_CHECKLIST_QUERY_03);

		List<ArticleData> checkListAnswers = getSimpleJdbcTemplate().query(
				query1.toString(), new ParameterizedRowMapper<ArticleData>() {
					public ArticleData mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ArticleData articleData = new ArticleData();
						articleData.setChecklistQuesSeqId(rs
								.getInt("CHECKLIST_QUESTION_SEQ_ID"));
						articleData.setAnswers(rs
								.getString("OWNER_ANSWER_STATUS"));
						return articleData;
					}
				}, Integer.parseInt(articleSeqId));
		for (ArticleData ad : checkListDetails) {
			for (ArticleData ans : checkListAnswers) {
				if (ans.getChecklistQuesSeqId() == ad.getChecklistQuesSeqId()) {
					ad.setSelectVal(ans.getAnswers());
				}
			}
		}
		// }
		query = null;// heap issue
		query1 = null;// heap issue
		return checkListDetails;
	}

	/**
	 * @param articleSeqId
	 * @param checklistQuesNoLst
	 * @param checkListAnswerLst
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String saveArticleCheckList(String articleSeqId,
			List<Integer> checklistQuesNoLst, List<String> checkListAnswerLst,
			List<Integer> checkListSeqIdLst) throws DRBCommonException {
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		final String userName = userObj.getUserFirstName() + ", "
				+ userObj.getUserLastName();
		String retVal = null;
		StringBuffer statusQuery = null;
		statusQuery = new StringBuffer();
		statusQuery.append(DRBQueryConstants.CHECKLIST_STATUS_QUERY);
		List<Integer> checkToUpdate = getSimpleJdbcTemplate().query(
				statusQuery.toString(), new ParameterizedRowMapper<Integer>() {
					public Integer mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return rs.getInt(1);
					}
				}, Integer.parseInt(articleSeqId));

		final List<Integer> checkListQno = checklistQuesNoLst;
		final List<String> checkListAns = checkListAnswerLst;
		final List<Integer> checkListSeqids = checkListSeqIdLst;
		final String articleSeqidFinal = articleSeqId;
		final int count = checkListAns.size();
		if (checkToUpdate.get(0) > 0) {
			StringBuffer updateQuery = null;
			updateQuery = new StringBuffer();
			updateQuery.append(DRBQueryConstants.CHECKLIST_DELETE_QUERY);
			getJdbcTemplate().update(updateQuery.toString(),
					new Object[] { Integer.parseInt(articleSeqId) });
			updateQuery = null;// heap issue
		}
		int[] rowsUpdatedByEachStatement = getSimpleJdbcTemplate()
				.getJdbcOperations().batchUpdate(
						DRBQueryConstants.SAVE_CHECKLIST_QUERY,
						new BatchPreparedStatementSetter() {
							public void setValues(PreparedStatement ps,
									int addCheckList) throws SQLException {
								ps.setInt(1, checkListSeqids.get(addCheckList));
								ps.setString(2, articleSeqidFinal);
								ps.setString(3, checkListAns.get(addCheckList));
								ps.setInt(4, checkListQno.get(addCheckList));
								ps.setString(5, userName);
								ps.setString(6, userName);
							}

							public int getBatchSize() {
								return count;
							}
						});
		if (rowsUpdatedByEachStatement != null
				&& rowsUpdatedByEachStatement.length == count)
			retVal = ArticleConstants.SAVE_SUCCESS;
		else
			retVal = ArticleConstants.SAVE_FAILURE;
		statusQuery = null;// heap issue

		return retVal;

	}

	/**
	 * @param articleSeqId
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String resetArticleCheckList(String articleSeqId)
			throws DRBCommonException {
		int val = 0;
		String retVal = null;
		StringBuffer statusQuery = new StringBuffer();
		statusQuery.append(DRBQueryConstants.CHECKLIST_STATUS_QUERY);
		List<Integer> checkToDelete = getSimpleJdbcTemplate().query(
				statusQuery.toString(), new ParameterizedRowMapper<Integer>() {
					public Integer mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return rs.getInt(1);
					}
				}, Integer.parseInt(articleSeqId));
		if (checkToDelete.get(0) > 0) {
			StringBuffer updateQuery = new StringBuffer();
			updateQuery.append(DRBQueryConstants.CHECKLIST_DELETE_QUERY);
			val = getJdbcTemplate().update(updateQuery.toString(),
					new Object[] { Integer.parseInt(articleSeqId) });
			if (val > 0) {
				retVal = ArticleConstants.RESET_SUCCESS;
			} else {
				// Log.info("Error while reset the article Checklist ::");
				retVal = ArticleConstants.RESET_FAILURE;
			}
			updateQuery = null;// heap issue
		} else {
			retVal = ArticleConstants.RESET_SUCCESS;
		}

		statusQuery = null;// heap issue
		return retVal;
	}

	/**
	 * @param articleSeqId
	 * @return List<ArticleData>
	 */
	public List<ArticleData> viewCheckListDeatils(String articleSeqId)
			throws DRBCommonException {
		// Log.info("inside the DAO Before Showing the checklist");

		StringBuffer queryForView = new StringBuffer();
		queryForView.append(DRBQueryConstants.VIEW_CHECKLIST_QUERY);
		List<ArticleData> viewCheckList = getSimpleJdbcTemplate().query(
				queryForView.toString(),
				new ParameterizedRowMapper<ArticleData>() {
					public ArticleData mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						ArticleData articleData = new ArticleData();
						articleData.setQuestions(rs.getString("QUESTION_TEXT"));
						articleData.setSelectVal(rs
								.getString("OWNER_ANSWER_STATUS"));
						articleData.setQuesorder(rowNum + 1);
						return articleData;
					}
				}, Integer.parseInt(articleSeqId));
		queryForView = null;// heap issue
		return viewCheckList;
	}

	// saurabh methods ends here

	/**
	 * @return the createArticlePrc
	 */
	public CreateArticleProcedure getCreateArticlePrc() {
		return _createArticlePrc;
	}

	/**
	 * @param createArticlePrc
	 *            the createArticlePrc to set
	 */
	public void setCreateArticlePrc(CreateArticleProcedure createArticlePrc) {
		this._createArticlePrc = createArticlePrc;
	}

	/********************** NEED TO KNOW ACCESS @GANESH ***************************/

	/**
	 * @author SSO This method is used to search the Article owner properties.
	 * @param userName
	 * @param itemid
	 * @param seqId
	 * @param type
	 * @return List<SelectItem>
	 * @throws DRBCommonException
	 */
	public List<SelectItem> searchIndividualUsers(String userName,
			String itemid, String seqId, String type) throws DRBCommonException {
		List<UserData> listOfUsers = null;// new ArrayList<UserData>();
		List<SelectItem> searchUsers = new ArrayList<SelectItem>();
		String ownerSSO = "";
		SelectItem selectItem = null;
		listOfUsers = _userServiceObj.getUsersInLibrary(userName, itemid);
		String toType = "";
		if ((ArticleConstants.ARTICLE1).equalsIgnoreCase(type)) {
			toType = ArticleConstants.OWNER;
		} else {
			toType = ArticleConstants.FOLDER;
			/** Ended by Atul **/
		}
		List<String> ownerID = getSSOIDs(seqId, toType);

		if (ownerID != null && ownerID.size() > 0) {
			ownerSSO = ownerID.get(0);
		}
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		if (userObj != null && listOfUsers != null) {
			for (UserData userList : listOfUsers) {
				if (!userList.getUserSSOID().equals(ownerSSO)) {
					selectItem = new SelectItem(userList.getUserLastName()
							+ ", " + userList.getUserFirstName() + "~"
							+ userList.getUserSSOID(),
							userList.getUserLastName() + ", "
									+ userList.getUserFirstName() + " ("
									+ userList.getUserSSOID() + ")");
					searchUsers.add(selectItem);
				}
			}
		}
		listOfUsers = null;// heap issue
		return searchUsers;
	}

	/**
	 * @param groupName
	 * @param itemid
	 * @return List<SelectItem>
	 */
	public List<SelectItem> searchGroupUsers(String groupName, String itemid) {
		String SEARCH_USERS = DRBQueryConstants.GROUPSEARCH_NTK;
		List<SelectItem> searchUsers = getSimpleJdbcTemplate().query(
				SEARCH_USERS, new ParameterizedRowMapper<SelectItem>() {
					public SelectItem mapRow(ResultSet rs, int arg1)
							throws SQLException {
						SelectItem selectItem = new SelectItem(rs
								.getString("GROUP_NAME")
								+ "~"
								+ rs.getString("LIBRARY_GROUP_SEQ_ID"), rs
								.getString("GROUP_NAME"));

						return selectItem;
					}
				}, itemid);
		return searchUsers;
	}

	/**
	 * @param selectedList
	 * @param type
	 * @return List<ArticleFolderData>
	 */
	public List<ArticleFolderData> userDetails(List<String> selectedList,
			String type) throws DRBCommonException {
		List<ArticleFolderData> us = new ArrayList<ArticleFolderData>();
		ArticleFolderData ud = null;
		if ("indUser".equalsIgnoreCase(type)) {
			/******** preparing List *************/
			List<String> preUserList = new ArrayList<String>();
			for (String user : selectedList) {
				String userid = user.substring(user.indexOf('(') + 1,
						user.indexOf(')'));
				preUserList.add(userid);
			}
			List<UserData> tempData = _userServiceObj
					.getUserDetails(preUserList);
			for (UserData user : tempData) {
				ud = new ArticleFolderData();
				ud.setUserSSOID(user.getUserLastName() + ", "
						+ user.getUserFirstName());
				ud.setUserRealId(user.getUserSSOID());
				ud.setAccessFrom(simple.format(new Date()));
				String temp = (String) user.getUserProperty().get(
						DRBUtils.getUserPropertyName("USER_COMPANY"));
				String tempCompany = (String) user.getUserProperty().get(
						DRBUtils.getUserPropertyName("USER_EMPLOYEE_TYPE"));
				if (ArticleConstants.GE_COMPANY.equalsIgnoreCase(temp)
						&& ReportConstants.EMPLOYEE
								.equalsIgnoreCase(tempCompany)) {
					ud.setEmpGe(true);
				} else {
					ud.setEmpGe(false);
				}
				us.add(ud);
			}
		} else {
			for (String user : selectedList) {
				ud = new ArticleFolderData();
				String userid = user.substring(user.indexOf('(') + 1,
						user.indexOf(')'));
				String name = user.substring(0, user.indexOf('('));
				ud.setUserSSOID(name);
				ud.setUserRealId(userid);
				ud.setAccessFrom(simple.format(new Date()));

				us.add(ud);
			}
		}
		return us;
	}

	/**
	 * @param requestFolderArticleData
	 * @param libId
	 * @param userId
	 * @return List<RequestGrantFolderArticleData>
	 */
	public List<RequestGrantFolderArticleData> requestArticleFolderAccess(
			List<RequestGrantFolderArticleData> requestFolderArticleData,
			String libId, String userId, boolean execute)
			throws DRBCommonException {
		// Log.info("Checking Values with Articles::::::::::::::::::::::In DAO");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		int count = 0;
		// Log.info("LIB ID------->" + libId + "   User SSO ID--------> " +
		// userId);
		for (RequestGrantFolderArticleData data : requestFolderArticleData) {
			count = 0;
			// Log.info("Article_Folder Seq Id >>> " +
			// data.getArticleFolderSeqNo());
			// Log.info("Article_Folder Code  >>>" +
			// data.getArticle_Folder_Number());
			// Log.info("Article_Folder Comments >>>" + data.getComments());
			// Log.info("Article Folder Title>>>>" + data.getArticle_Folder());
			// Log.info("Type >>>>>>" + data.getType());
			// Log.info("Tagger ID>>>>>>>>>" + data.getTaggerId());
			StringBuffer userid = new StringBuffer();
			StringBuffer userAccessUpto = new StringBuffer();
			StringBuffer groupid = new StringBuffer();
			StringBuffer groupAccessUpto = new StringBuffer();
			String taggerSSO = null;
			if (data.getTaggerId() != null) {
				taggerSSO = data.getTaggerId().substring(
						data.getTaggerId().indexOf('(') + 1,
						data.getTaggerId().indexOf(')'));
			}
			// Log.info("Tagger ID>>>>>>>>>" + taggerSSO);
			for (ArticleFolderData indUser : data.getAccesstoIndividualUsers()) {
				userid.append(indUser.getUserRealId()).append("~");
				if (indUser.getAccessUpto() == null) {
					userAccessUpto.append("").append("~");
				} else {
					userAccessUpto.append(
							dateFormat.format(indUser.getAccessUpto())).append(
							"~");
				}
				count = 1;
			}
			for (ArticleFolderData groupUser : data.getAccesstoUserGroups()) {
				groupid.append(groupUser.getUserRealId()).append("~");
				if (groupUser.getAccessUpto() == null) {
					groupAccessUpto.append("").append("~");
				} else {
					groupAccessUpto.append(
							dateFormat.format(groupUser.getAccessUpto()))
							.append("~");
				}
				count = 1;
			}
			if (count == 1 || execute) {
				// Log.info("User ID >>>>" + userid);
				// Log.info("User Access Upto >>>" + userAccessUpto);
				// Log.info("Group ID >>>>" + groupid);
				// Log.info("Group Access Upto >>>>" + groupAccessUpto);

				Map outparm = _checkTaggingProcedure.executeCheckTagging(
						userId, libId, data.getArticleFolderSeqNo(),
						data.getArticleFolderNumber(), data.getArticleFolder(),
						taggerSSO, data.getType(), userid.toString(),
						userAccessUpto.toString(), groupid.toString(),
						groupAccessUpto.toString(), data.getComments());
				// Log.info("Out Put Value-------------->" + (String)
				// outparm.get("param") + "   size      " + outparm.size());
				String outParm = (String) outparm.get("param");
				if (outParm != null) {
					// Log.info("Depending on OUTPARM ---->Action will be done");
					/****************************************
					 * Email Has to go depending on Out put from PRC
					 **********************************************/
					data.setMailStatus(outParm);
				}
			}
		}

		/******* DataBase has to be updated ***************/
		/**** Request has to be Raised for each user *********/
		return requestFolderArticleData;
	}

	/**
	 * @param articleDetaisls
	 * @param folderDetaisls
	 * @return List<RequestGrantFolderArticleData>
	 */
	public List<RequestGrantFolderArticleData> getArticleFolderDetails(
			String articleDetaisls, String folderDetaisls, boolean ectQuery) {
		StringBuffer GET_ARTICLEDETAIS = null;
		GET_ARTICLEDETAIS = new StringBuffer();
		StringBuffer GET_FOLDERDETAIS = null;
		GET_FOLDERDETAIS = new StringBuffer();

		if (ectQuery) {
			GET_ARTICLEDETAIS.append(DRBQueryConstants.GET_ECT_ARTICLEDETAIS);
			GET_FOLDERDETAIS.append(DRBQueryConstants.GET_ECT_FOLDERDETAIS);
		} else {
			GET_ARTICLEDETAIS
					.append("SELECT ARTICLE_CODE,ARTICLE_TITLE,ARTICLE_SEQ_ID,LEGACY_ARTICLE_CODE,"
							+ "LEGACY_IND,OWNER_USER_ID,"
							+ "(SELECT STATE_NAME FROM CAWC.DRB_WF_STATE WHERE WF_STATE_SEQ_ID = CAWC.DRB_ARTICLE.ARTICLE_WF_STATE_SEQ_ID) "
							+ "STATE_NAME FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID IN ");
			GET_FOLDERDETAIS
					.append("SELECT FOLDER_CODE,FOLDER_TITLE,FOLDER_SEQ_ID,LOCK_IND,DELETE_IND,"
							+ "LEGACY_FOLDER_CODE,LEGACY_IND,"
							+ "OWNER_USER_ID FROM CAWC.DRB_FOLDER WHERE FOLDER_SEQ_ID IN ");
		}

		List<RequestGrantFolderArticleData> totalSelectedList = null;
		List<RequestGrantFolderArticleData> articlesList = null;
		List<RequestGrantFolderArticleData> folderList = null;
		StringBuffer setIndUsers = new StringBuffer();
		if (articleDetaisls != null && !articleDetaisls.equals("")) {
			String[] articles = articleDetaisls
					.split(ArticleConstants.EDRB_SEPARATOR);
			for (String articleSeqNo : articles) {
				setIndUsers.append(ArticleConstants.EDRB_SEPARATOR).append(
						articleSeqNo);
			}
		}
		if (setIndUsers.length() > 0 && !"".equals(setIndUsers.toString())) {
			// Log.info(GET_ARTICLEDETAIS + " (" + setIndUsers.substring(1,
			// setIndUsers.length()) + ")");
			String GETARTICLEDETAILS = GET_ARTICLEDETAIS.toString() + " ("
					+ setIndUsers.substring(1, setIndUsers.length()) + ")";
			/****** article details *************/
			articlesList = getSimpleJdbcTemplate()
					.query(GETARTICLEDETAILS,
							new ParameterizedRowMapper<RequestGrantFolderArticleData>() {
								public RequestGrantFolderArticleData mapRow(
										ResultSet rs, int arg1)
										throws SQLException {
									RequestGrantFolderArticleData articles = new RequestGrantFolderArticleData();
									articles.setArticleFolder(rs
											.getString("ARTICLE_TITLE"));
									articles.setArticleFolderNumber(rs
											.getString("ARTICLE_CODE"));
									articles.setArticleFolderSeqNo(rs
											.getString("ARTICLE_SEQ_ID"));
									articles.setImage("../images/article.gif");
									articles.setType("ARTICLE");
									articles.setInvdUserData(true);
									articles.setGroupUserData(true);
									articles.setLegacyCode(rs
											.getString("LEGACY_ARTICLE_CODE"));
									articles.setLegacyInd(rs
											.getString("LEGACY_IND"));
									articles.setOwnerId(rs
											.getString("OWNER_USER_ID"));
									articles.setArticleState(rs
											.getString("STATE_NAME"));
									return articles;
								}
							});
		}
		StringBuffer setFolderUser = new StringBuffer();
		if (folderDetaisls != null && !folderDetaisls.equals("")) {
			String[] folders = folderDetaisls
					.split(ArticleConstants.EDRB_SEPARATOR);
			for (String foldersSeqNo : folders) {
				setFolderUser.append(ArticleConstants.EDRB_SEPARATOR).append(
						foldersSeqNo);
			}
		}
		if (setFolderUser.length() > 0 && !"".equals(setFolderUser.toString())) {
			// Log.info(GET_FOLDERDETAIS + " (" + setFolderUser.substring(1,
			// setFolderUser.length()) + ")");
			String GETFOLDERDETAILS = GET_FOLDERDETAIS + " ("
					+ setFolderUser.substring(1, setFolderUser.length()) + ")";
			/**************** folder details ****************/
			folderList = getSimpleJdbcTemplate()
					.query(GETFOLDERDETAILS,
							new ParameterizedRowMapper<RequestGrantFolderArticleData>() {
								public RequestGrantFolderArticleData mapRow(
										ResultSet rs, int arg1)
										throws SQLException {
									RequestGrantFolderArticleData folders = new RequestGrantFolderArticleData();
									folders.setArticleFolder(rs
											.getString("FOLDER_TITLE"));
									folders.setArticleFolderNumber(rs
											.getString("FOLDER_CODE"));
									folders.setArticleFolderSeqNo(rs
											.getString("FOLDER_SEQ_ID"));
									String subQuery = DRBQueryConstants.ARTICLE_COUNT_FOLDER;
									List<Integer> subQueryExc = getSimpleJdbcTemplate()
											.query(subQuery,
													new ParameterizedRowMapper<Integer>() {
														public Integer mapRow(
																ResultSet rsSub,
																int arg)
																throws SQLException {
															return rsSub
																	.getInt(1);
														}
													},
													rs.getString("FOLDER_SEQ_ID"));
									int countArticle = 0;
									if (subQueryExc != null
											&& subQueryExc.size() > 0) {
										countArticle = subQueryExc.get(0);
									}
									if (countArticle == 0
											&& "Y".equalsIgnoreCase(rs
													.getString("LOCK_IND"))) {
										folders.setImage("../images/folder-lock.gif");
									} else if (countArticle == 0) {
										folders.setImage("../images/folder-empty.gif");
									} else if ("Y".equalsIgnoreCase(rs
											.getString("LOCK_IND"))) {
										folders.setImage("../images/folder-lock.gif");
									} else {
										folders.setImage("../images/folder-closed.gif");
									}
									folders.setType("FOLDER");
									folders.setInvdUserData(true);
									folders.setGroupUserData(true);
									folders.setLegacyCode(DRBUtils.checkNullVal(rs
											.getString("LEGACY_FOLDER_CODE")));
									folders.setLegacyInd(rs
											.getString("LEGACY_IND"));
									folders.setOwnerId(rs
											.getString("OWNER_USER_ID"));
									return folders;
								}
							});
		}

		totalSelectedList = articlesList;
		if (totalSelectedList == null) {
			totalSelectedList = new ArrayList<RequestGrantFolderArticleData>();
		}
		if (folderList != null) {
			for (RequestGrantFolderArticleData dump : folderList) {
				totalSelectedList.add(dump);
			}
		}

		if (totalSelectedList != null) {
			for (RequestGrantFolderArticleData dump : totalSelectedList) {
				// Log.info("---------------" + dump.getArticle_Folder() +
				// "-------" + dump.getArticleFolderSeqNo() + "-----"
				// + dump.getArticle_Folder_Number());
			}
		}
		return totalSelectedList;
	}

	/**
	 * @param requestFolderArticleData
	 * @param libId
	 * @param userId
	 * @return List<RequestGrantFolderArticleData>
	 */
	public List<RequestGrantFolderArticleData> checkingArticleFolder(
			List<RequestGrantFolderArticleData> requestFolderArticleData,
			String libId, String userId) {
		String CHECKING_ARTICLE = DRBQueryConstants.CHECKING_PENDING_ARTICLE;
		String CHECKING_FOLDER = DRBQueryConstants.CHECKING_PENDING_FOLDER;
		StringBuffer notValid = new StringBuffer();
		List<RequestGrantFolderArticleData> removePending = new ArrayList<RequestGrantFolderArticleData>();
		for (RequestGrantFolderArticleData data : requestFolderArticleData) {
			List<Integer> checkArticleFolder = null;
			if ("Article".equalsIgnoreCase(data.getType())) {
				checkArticleFolder = getSimpleJdbcTemplate().query(
						CHECKING_ARTICLE,
						new ParameterizedRowMapper<Integer>() {
							public Integer mapRow(ResultSet rs, int arg1)
									throws SQLException {
								return rs.getInt(1);
							}
						}, data.getArticleFolderSeqNo(), userId, libId,
						"Pending");
			} else {
				checkArticleFolder = getSimpleJdbcTemplate().query(
						CHECKING_FOLDER,
						new ParameterizedRowMapper<Integer>() {
							public Integer mapRow(ResultSet rs, int arg1)
									throws SQLException {
								return rs.getInt(1);
							}
						}, data.getArticleFolderSeqNo(), userId, libId,
						"Pending");
			}
			if (checkArticleFolder != null && checkArticleFolder.size() > 0) {
				if (checkArticleFolder.get(0) >= 1) {
					notValid.append(data.getArticleFolderNumber()).append("\n");
					/*****************************************
					 * To be Removed List
					 ***************************************/
					removePending.add(data);
				}
			}
		}
		// Log.info(notValid);
		return removePending;
	}

	/**
	 * @param userIds
	 * @return List<String>
	 */
	public List<String> getMailIds(List<String> userIds) {
		String queryForMailId = "SELECT REALEMAIL FROM CAWC.DRB_USER WHERE UNAME IN (";
		// Log.info("queryForMailId::::::" + DRBUtils.setListForQuery(userIds));
		queryForMailId += DRBUtils.setListForQuery(userIds) + ")";
		List<String> mailIds = getSimpleJdbcTemplate().query(queryForMailId,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("REALEMAIL");
					}
				});
		return mailIds;
	}

	/**
	 * This method gets the Owner for the Article or folder. //javeed 23-Dec-10
	 * 
	 * @param seqId
	 * @param type
	 * @return List<String>
	 */
	public List<String> getSSOIDs(String seqId, String type) {
		String queryForSSOID = "";
		if (ArticleConstants.FOLDER.equalsIgnoreCase(type)) { // javeed
																// 23-Dec-10
																// Def_No :1245
			queryForSSOID = DRBQueryConstants.NTK_FODLER_OWNER;
		} else if (ArticleConstants.PARENT.equalsIgnoreCase(type)) { // javeed
																		// 23-Dec-10
			queryForSSOID = DRBQueryConstants.NTK_PARENT_OWNER;
		} else if (ArticleConstants.OWNER.equalsIgnoreCase(type)) { // javeed
																	// 23-Dec-10
			queryForSSOID = DRBQueryConstants.NTK_ARTICLE_OWNER;
		}
		List<String> IDS = getSimpleJdbcTemplate().query(queryForSSOID,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("OWNER_USER_ID");
					}
				}, seqId);
		return IDS;
	}

	/**** Added By Ganesh *********/
	/**
	 * @param articleNo
	 * @return
	 */
	public String reviewFlagCheck(String articleNo) {
		String SEARCH_USERS = "SELECT ARTICLE_IN_REVIEW FROM CAWC.DRB_ARTICLE WHERE ARTICLE_SEQ_ID=?";
		List<String> flagCheck = getSimpleJdbcTemplate().query(SEARCH_USERS,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("ARTICLE_IN_REVIEW");
					}
				}, articleNo);
		return flagCheck.get(0);
	}

	public String getExportControlTag(String articleNo)
			throws DRBCommonException {
		String GET_TAG = "select EXPORT_CLASS_NAME from CAWC.DRB_ARTICLE ART,CAWC.DRB_EXPORT_CLASS EXP where Article_seq_id=? "
				+ "and ART.EXPORT_CLASS_ID = EXP.EXPORT_CLASS_ID";
		List<String> flagCheck = getSimpleJdbcTemplate().query(GET_TAG,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("EXPORT_CLASS_NAME");
					}
				}, articleNo);

		if (flagCheck != null && flagCheck.size() > 0) {
			return flagCheck.get(0);
		} else {
			return null;
		}
	}

	public String getNameTagger(String reqEventSeqId, String assetType)
			throws DRBCommonException {
		String qry = DRBQueryConstants.GET_ARTICLE_NAMED_TAGGER;
		if (assetType.equalsIgnoreCase(ECTagConstants.FOLDER)) {
			qry = DRBQueryConstants.GET_FOLDER_NAMED_TAGGER;
		}

		List<String> flagCheck = getSimpleJdbcTemplate().query(qry,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("NAMED_TAGGER_USER_ID");
					}
				}, reqEventSeqId);
		if (flagCheck != null && flagCheck.size() > 0) {
			return flagCheck.get(0);
		} else {
			return null;
		}
	}

	public String getLocation(String filaname, String artNum) {
		List<String> loc = getSimpleJdbcTemplate().query(
				DRBQueryConstants.GET_LOCATION_PATH,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("NETWORK_PATH");
					}
				}, filaname, artNum);
		String locPath = "";
		if (loc != null && loc.size() > 0) {
			locPath = loc.get(0);
		}
		// Log.info("In Dao >>>>>>>>>>>getLocation" + locPath);
		return locPath;
	}

	public int getFileExists(String filaname, String artNum) {
		int appTableSeqid = 0;
		appTableSeqid = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_APP_TABLE_SEQ_ARTICLE);
		int documentRevision = NEGATIVEINDEX;
		documentRevision = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_DOCUMENT_REVISION, filaname, artNum,
				appTableSeqid);
		// Log.info("In Dao >>>>>>>>>>>getFileExists" + documentRevision);
		return documentRevision;
	}

	/**
	 * @param articleNo
	 * @param role
	 * @return
	 */
	public String reviewFlagUpdate(String articleNo, String role) {
		String UPDATE_FLAG = "UPDATE  CAWC.DRB_ARTICLE SET ARTICLE_IN_REVIEW=? WHERE ARTICLE_SEQ_ID=?";
		getSimpleJdbcTemplate().update(UPDATE_FLAG, role, articleNo);
		return "updated";
	}

	// Method added by Goutam for Upload files
	/**
	 * @param userID
	 * @param articleID
	 * @param documentType
	 * @param fileName
	 * @param file
	 * @return boolean
	 * @roseuid 494F65AB00D4
	 */
	@Transactional
	public boolean uploadArticleDocumentDAO(String userID, String articleID,
			String fileSizeStr, java.lang.String fileName, String userFullName,
			String filePath) throws DRBCommonException {
		int locationID = 0;
		int locationIDCount = 0;
		int dataClassificationID = 0;
		int documentRevision = ArticleConstants.MINUS_ONE;
		int appTableID = 0;
		int articleSeqID = Integer.parseInt(articleID);
		int docSeqID = 0;
		int docOrder = 0;

		locationIDCount = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_LOCATION_SEQ_COUNT, filePath);

		if (locationIDCount == 0) {
			locationID = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.GET_NEW_LOCATION_SEQ);
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.INSERT_DOCUMENT_LOCATION, locationID,
					filePath, userID, userID);

		} else {
			locationID = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.GET_LOCATION_SEQ, filePath);
		}

		// Log.info("the locationID is------" + locationID);

		dataClassificationID = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_DOCUMENT_DATA_CLASS_ID, articleSeqID);
		// Log.info("the dataClassificationID is------" + dataClassificationID);

		appTableID = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_APP_TABLE_SEQ_ARTICLE);
		// Log.info("the appTableID is------" + appTableID);

		docOrder = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_DOCUMENT_ORDER, articleSeqID, appTableID);
		// Log.info("the docOrder is------" + docOrder);

		documentRevision = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.GET_DOCUMENT_REVISION, fileName,
				articleSeqID, appTableID);
		// Log.info("the documentRevision is------" + documentRevision);

		if (documentRevision != 0) {
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_DOCUMENT_INFO, userFullName,
					fileName, articleSeqID);
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_SIZE_DOCUMENT_PROPERTY,
					fileSizeStr, articleSeqID, fileName);
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.DELETE_DOCUMENT_PROPERTY, articleSeqID,
					fileName, ArticleConstants.VERIFICATION_SCORE,
					ArticleConstants.VERIFICATION_DESC);

			// Log.info("the documentRevision in update is------" +
			// documentRevision);
		} else {

			// For different revisions check for duplicate records.
			String docTitle = fileName.substring(fileName.indexOf("~") + 1);
			String toUpdateDOCSeqId = "";
			// String[] theArr = fileName.split("~");
			/*
			 * if(theArr != null){ docTitle = theArr[1]; }
			 */
			List<DocumentDetailsData> theList = getAttachmentDetails(String
					.valueOf(articleSeqID));
			if (theList != null && !theList.isEmpty()) {
				for (int j = 0; j < theList.size(); j++) {
					DocumentDetailsData theDocumentDetailsData = theList.get(j);
					if (theDocumentDetailsData.getDocumentTitle()
							.equalsIgnoreCase(docTitle)) {
						toUpdateDOCSeqId = theDocumentDetailsData
								.getDocumentSeqId();
						break;
					}
				}
			}
			if (toUpdateDOCSeqId != null && !"".equals(toUpdateDOCSeqId)) {
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_DOCUMENT_NAME, userFullName,
						fileName, locationID, toUpdateDOCSeqId);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.DELETE_DOCUMENT_PROPERTY,
						articleSeqID, fileName,
						ArticleConstants.VERIFICATION_SCORE,
						ArticleConstants.VERIFICATION_DESC);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_SIZE_DOCUMENT_PROPERTY1,
						userFullName, fileSizeStr, articleSeqID, fileName);
			} else {

				// Log.info("the documentRevision is------" + documentRevision);
				documentRevision = 0;
				docSeqID = getSimpleJdbcTemplate().queryForInt(
						DRBQueryConstants.GET_DOCUMENT_SEQ_ID);
				// Log.info("the documentRevision is Done1------" +
				// DRBQueryConstants.INSERT_DOCUMENT_INFO);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.INSERT_DOCUMENT_INFO, docSeqID,
						fileName,
						fileName.substring(fileName.indexOf('~') + 1),
						documentRevision, userID, articleSeqID, appTableID,
						dataClassificationID, locationID, docOrder + 1,
						userFullName, userFullName);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.INSERT_DOCUMENT_PROPERTY,
						userFullName, userFullName,
						ReportConstants.CREATED_BY_ID, userID, docSeqID);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.INSERT_DOCUMENT_PROPERTY,
						userFullName, userFullName, ReportConstants.FILE_SIZE,
						fileSizeStr, docSeqID);
				// Log.info("the documentRevision is Done2------");
			}
		}
		return true;
	}

	/* delete and reorder attachments by surya goutham */

	/**
	 * @param articleNo
	 * @return List<ArticleData>
	 * @throws DRBCommonException
	 */
	public List<DocumentDetailsData> getAttachmentDetails(String articleNo)
			throws DRBCommonException {
		// Log.info(">>>>>>>>>>>>>>>>>>>>>in DAO  getting attachment details");
		int app_id = Integer.parseInt(articleNo);
		List<DocumentDetailsData> attachmentDetailsList = getSimpleJdbcTemplate()
				.query(DRBQueryConstants.ATTACHMENT_DETAILS_BY_ARTICLE_SEQ_ID,
						new ParameterizedRowMapper<DocumentDetailsData>() {
							public DocumentDetailsData mapRow(ResultSet rs,
									int rowNum) throws SQLException {
								DocumentDetailsData attachment = new DocumentDetailsData();
								attachment.setDocumentName(rs
										.getString("DOCUMENT_NAME"));
								attachment.setDocumentTitle(rs
										.getString("DOCUMENT_TITLE"));
								attachment.setDocumentSeqId(rs
										.getString("DOCUMENT_SEQ_ID"));
								attachment.setDocumentDescription(rs
										.getString("DOCUMENT_DESCRIPTION"));
								// ArticleConstants.ONE_NINE_NINE)
								attachment.setReorderNumber(rs
										.getString("DOCUMENT_ORDER"));
								attachment.setNetworkPath(rs
										.getString("NETWORK_PATH"));
								// FUTURE CODE
								/*
								 * if("Y".equals(rs.getString("ARCHIVE_IND"))){
								 * attachment.setArticleArchiveInd(true); }else{
								 * attachment.setArticleArchiveInd(false); }
								 */
								return attachment;
							}
						}, app_id);
		return attachmentDetailsList;
	}

	/**
	 * @param articleNumber
	 * @return
	 * @throws DRBCommonException
	 */
	public List<String> getAttachmentTitles(String articleNumber)
			throws DRBCommonException {
		List<String> documentTitles = new ArrayList<String>();
		List<Map<String, Object>> titles = getSimpleJdbcTemplate()
				.queryForList(
						DRBQueryConstants.ATTACHMENT_TITLES_BY_ARTICLE_SEQ_ID,
						articleNumber);
		if (titles.size() > 0)
			for (Map title : titles) {
				documentTitles.add(title.get("DOCUMENT_TITLE").toString());
			}
		return documentTitles;
	}

	/* delete and reorder attachments by surya goutham */

	/**
	 * @param articleNo
	 * @return List<ArticleData>
	 * @throws DRBCommonException
	 */
	public void removeOwnerTask(String articleNo) throws DRBCommonException {
		// Log.info(">>>>>>>>>>>>>>>>>>>>>in DAO  getting attachment details");
		int noOfRows = 0;
		try {
			noOfRows = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.COUNT_DOCUMENT_VERIFICATION_SCORE,
					articleNo, ArticleConstants.VERIFICATION_SCORE);
			if (noOfRows == 0) {
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.UPDATE_REQUESTEVENT_ATTCH_REMOVE_ARTICLE,
								ArticleConstants.CHAR_N,
								ArticleConstants.ATTACHMENT_TASK_TYPE,
								articleNo);
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught while checking for article removeOwnerTask for articleSeqId: "
					+ articleNo);
			LOG.info("Error is: " + ex.getMessage());
		}

	}

	/**
	 * This method is used to delete the Attached documents
	 * 
	 * @param articleNo
	 * @param attachmentNo
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String deleteDocuments(String articleNo, String[] attachmentNo)
			throws DRBCommonException {
		// Log.info(">>>>>>>>>>>>>>>>>>>>>>>> in dao deleting selected attachments ");
		int app_data_id = Integer.parseInt(articleNo);

		String[] deleteStatementsArray = new String[attachmentNo.length];
		String[] deleteStatementsArraydocprp = new String[attachmentNo.length];

		// Preparing statements to delete from drb_document
		for (int itr = 0; itr < attachmentNo.length; itr++) {
			String DRB_ATTACHMENTS_D_UPDATE = "DELETE  FROM CAWC.DRB_DOCUMENT WHERE APP_DATA_PK_ID='"
					+ app_data_id
					+ "' AND DOCUMENT_SEQ_ID='"
					+ attachmentNo[itr] + "'";
			deleteStatementsArray[itr] = DRB_ATTACHMENTS_D_UPDATE;
		}
		// Preparing statements to delete from drb_document_property
		for (int itr = 0; itr < attachmentNo.length; itr++) {
			String DRB_ATTACHMENTS_D_UPDATE = "DELETE  FROM CAWC.DRB_DOCUMENT_PROPERTY WHERE DOCUMENT_SEQ_ID='"
					+ attachmentNo[itr] + "'";
			deleteStatementsArraydocprp[itr] = DRB_ATTACHMENTS_D_UPDATE;
		}
		// deleting from drb_document_property
		getJdbcTemplate().batchUpdate(deleteStatementsArraydocprp);
		// deleting from drb_document
		getJdbcTemplate().batchUpdate(deleteStatementsArray);
		return ArticleConstants.ADD_ATTACHEMENTS;
	}

	/**
	 * This method is used to reorder the attached documents
	 * 
	 * @param articleNo
	 * @param attachmentNo
	 * @param orderNo
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String reorderDocuments(final String articleNo,
			final String[] attachmentNo, final String[] orderNo)
			throws DRBCommonException {
		// Log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>> in DAO  reorderting attachments ");
		String status = null;
		final int count = attachmentNo.length;
		UserData userObj = (UserData) (DRBUtils.getServletSession(true)
				.getAttribute("USER_DATA"));
		final String lastUpdatedBy = DRBUtils.setPersonnelFullName(
				userObj.getUserLastName(), userObj.getUserFirstName());

		int[] rowsUpdatedByEachStatement = getJdbcTemplate().batchUpdate(
				DRBQueryConstants.DRB_ATTACHMENTS_D_UPDATE,
				new BatchPreparedStatementSetter() {
					public void setValues(PreparedStatement ps,
							int addAttachment) throws SQLException {
						ps.setString(1, orderNo[addAttachment]);
						ps.setString(2, lastUpdatedBy);
						ps.setString(3, articleNo);
						ps.setString(4, attachmentNo[addAttachment]);

					}

					public int getBatchSize() {
						return count;
					}

				});

		if (rowsUpdatedByEachStatement != null
				&& rowsUpdatedByEachStatement.length == orderNo.length) {
			status = ArticleConstants.RESET_SUCCESS;
		} else {
			status = ArticleConstants.RESET_FAILURE;
		}
		return status;
	}

	/**
	 * This method is used to remove the attached amend Documents
	 * 
	 * @param List
	 * @param String
	 * @param reviewRole
	 * @throws DRBCommonException
	 */
	@Transactional
	public String removeAmendDocuments(List<String> amendToRemove,
			String articleSeqId, String reviewRole) throws DRBCommonException {
		// Log.info("inside the DAO for remove the amends===" + amendToRemove);
		int removeAmends = 0;
		String toUpdate = null;
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String loginName = userObj.getUserLastName()
				+ ArticleConstants.SEPARATOR + userObj.getUserFirstName();
		String articleCode = ArticleConstants.EMPTY;
		String amendedArtCode = ArticleConstants.EMPTY;
		if (amendToRemove.size() > 0) {
			toUpdate = "";
			for (int i = 0; i < amendToRemove.size(); i++) {
				String removeamend = amendToRemove.get(i);
				removeAmends = getJdbcTemplate().update(
						DRBQueryConstants.REMOVE_AMENDS,
						new Object[] { Integer.parseInt(removeamend),
								articleSeqId });
				articleCode = getSimpleJdbcTemplate().queryForObject(
						DRBQueryConstants.ART_CODE_HIST,
						new ParameterizedRowMapper<String>() {
							public String mapRow(ResultSet rs, int arg1)
									throws SQLException {
								return rs.getString("ARTICLE_CODE");
							}
						}, removeamend);
				// Log.info("the original seq id " + articleSeqId);
				// Log.info("the amend seq id " + removeamend);
				// added for capturing to history for removal of amends
				ArticleData articleData = new ArticleData();
				articleData.setOldValue("");
				articleData.setNewValue(articleCode);
				articleData.setChangedAttributeName("Remove Amends");
				articleData.setArticleSeqid(articleSeqId);
				captureArticlesHistory(articleData);
				// added for capturing to history for removal of amended
				amendedArtCode = getSimpleJdbcTemplate().queryForObject(
						DRBQueryConstants.ART_CODE_HIST,
						new ParameterizedRowMapper<String>() {
							public String mapRow(ResultSet rs, int arg1)
									throws SQLException {
								return rs.getString("ARTICLE_CODE");
							}
						}, articleSeqId);
				ArticleData articleObj = new ArticleData();
				articleObj.setOldValue("");
				articleObj.setNewValue(amendedArtCode);
				articleObj.setChangedAttributeName("Removal of Amended");
				articleObj.setArticleSeqid(removeamend);
				captureArticlesHistory(articleObj);
			}
		}
		if (reviewRole != null && removeAmends > 0
				&& reviewRole.trim().endsWith(ArticleConstants.EDRB_ROLE_03)) {

			ParameterizedRowMapper<List<String>> mapper = new ParameterizedRowMapper<List<String>>() {
				public List<String> mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					List<String> list = new ArrayList<String>();
					list.add(rs.getString("REVIEWER_METADATA_EDIT_IND"));
					list.add(rs.getString("REVIEWER_METADATA_EDIT"));
					return list;
				}
			};
			List<List<String>> returnList = getSimpleJdbcTemplate().query(
					DRBQueryConstants.IS_ARTICLE_EDITED, mapper,
					new Object[] { articleSeqId });
			if (returnList != null && returnList.size() > 0) {
				String metaDataInd = returnList.get(0).get(0);
				String metaDataEdit = returnList.get(0).get(1);
				boolean updateFlag = false;
				if (metaDataEdit != null) {
					String metaDataSplit[] = metaDataEdit.split(",");
					for (int itr = 0; itr < metaDataSplit.length; itr++) {
						updateFlag = metaDataSplit[itr]
								.equalsIgnoreCase("Amends");
						if (updateFlag) {
							// Log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%update flag%%%% "
							// + updateFlag);
							break;
						}
					}
				}
				if ("".equals(metaDataEdit)) {
					metaDataEdit = "Amends,";
				} else if (metaDataEdit != null && updateFlag == false) {
					metaDataEdit = metaDataEdit + "Amends,";
				}
				// Log.info("Amends remove sucessfully remove Role is =======>"
				// + reviewRole);
				toUpdate = "";

				// Log.info(metaDataInd);
				// Code Review Changed By Naresh on 22DEC10 Redundant nullcheck
				// of metaDataEdit Start
				if ("".equals(metaDataInd) || "N".equalsIgnoreCase(metaDataInd)) {
					if (reviewRole
							.equalsIgnoreCase(ArticleConstants.EDRB_ROLE_03)) {
						toUpdate = "P";
					} else {
						toUpdate = "A";
					}
				} else if ("".equals(metaDataInd)
						|| "P".equalsIgnoreCase(metaDataInd)) {
					if (reviewRole
							.equalsIgnoreCase(ArticleConstants.EDRB_ROLE_03)) {
						toUpdate = "P";
					} else {
						toUpdate = "Y";
					}
				} else if ("".equals(metaDataInd)
						|| "A".equalsIgnoreCase(metaDataInd)) {
					if (reviewRole
							.equalsIgnoreCase(ArticleConstants.EDRB_ROLE_03)) {
						toUpdate = "Y";
					} else {
						toUpdate = "A";
					}
				}
				if ("".equals(metaDataInd) || "Y".equalsIgnoreCase(metaDataInd)) {
					toUpdate = "Y";
				}
				// Code Review Changed By Naresh on 22DEC10 Redundant nullcheck
				// of metaDataEdit End
				// Log.info("%%%%%%%%%%%%%%remove amennds%%%%%%%%%%the final metadata edit %%%% "
				// + metaDataEdit);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_REVIEWER_METADATA_IND,
						toUpdate, metaDataEdit, loginName, articleSeqId);
			}
		}
		return toUpdate;
	}

	// added by santosh on 19 jan start
	public List getHistoryListChangeArticle(String articleSeqId, String userId)
			throws DRBCommonException {
		// LOG.info("***1111111111111111*****"+articleSeqId);
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String revSeqIdQuery = DRBQueryConstants.GET_REVISION_SEQ_ID;
		Object[] obj = new Object[] { articleSeqId };
		ArticleData theArticleDataObj = getSimpleJdbcTemplate().queryForObject(
				revSeqIdQuery, new ParameterizedRowMapper<ArticleData>() {
					public ArticleData mapRow(ResultSet rs, int arg1)
							throws SQLException {
						ArticleData theObj = new ArticleData();
						theObj.setRevisionArticleSeqId(rs
								.getString("REVISION_ARTICLE_SEQ_ID"));
						theObj.setArticleTitle(rs.getString("ARTICLE_TITLE"));
						return theObj;
					}
				}, obj);

		String revisionSeqId = theArticleDataObj.getRevisionArticleSeqId();
		String newArticleTitle = theArticleDataObj.getArticleTitle();
		if (revisionSeqId != null) {
			// LOG.info("***444*****"+newArticleTitle);
			// LOG.info("***33333*****"+revisionSeqId);
			Object[] objRev = new Object[] { revisionSeqId };
			ArticleData theArticleDataObjRev = getSimpleJdbcTemplate()
					.queryForObject(revSeqIdQuery,
							new ParameterizedRowMapper<ArticleData>() {
								public ArticleData mapRow(ResultSet rs, int arg1)
										throws SQLException {
									ArticleData theObj = new ArticleData();
									theObj.setRevisionArticleSeqId(rs
											.getString("REVISION_ARTICLE_SEQ_ID"));
									theObj.setArticleTitle(rs
											.getString("ARTICLE_TITLE"));
									return theObj;
								}
							}, objRev);

			String oldArticleTitle = theArticleDataObjRev.getArticleTitle();
			// LOG.info("***555*****"+oldArticleTitle);
			// Update curent revision history

			String userName1 = userObj.getUserLastName() + ", "
					+ userObj.getUserFirstName();
			getJdbcTemplate()
					.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_STATE_CHANGE_HISTORY_QUERY,
							new Object[] { userId, "Article Title Changed",
									oldArticleTitle, newArticleTitle,
									Integer.parseInt(articleSeqId), userName1,
									userName1 });

			while (revisionSeqId != null) {
				// LOG.info("**before a*777777777777*****");
				String titleQuery = DRBQueryConstants.UPDATE_ART_REV_TITLE_VALUE;
				Object[] titleObj = new Object[] { newArticleTitle,
						revisionSeqId };
				getSimpleJdbcTemplate().update(titleQuery, titleObj);
				// LOG.info("***777777777777*****");
				// Update history for the revisions

				getJdbcTemplate()
						.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_STATE_CHANGE_HISTORY_QUERY,
								new Object[] { userId, "Article Title Changed",
										oldArticleTitle, newArticleTitle,
										Integer.parseInt(revisionSeqId),
										userName1, userName1 });
				Object[] theObj = new Object[] { revisionSeqId };
				ArticleData theRevForRev = getSimpleJdbcTemplate()
						.queryForObject(revSeqIdQuery,
								new ParameterizedRowMapper<ArticleData>() {
									public ArticleData mapRow(ResultSet rs,
											int arg1) throws SQLException {
										ArticleData theObj = new ArticleData();
										theObj.setRevisionArticleSeqId(rs
												.getString("REVISION_ARTICLE_SEQ_ID"));
										theObj.setArticleTitle(rs
												.getString("ARTICLE_TITLE"));
										return theObj;
									}
								}, theObj);

				String revId = theRevForRev.getRevisionArticleSeqId();
				if (revId != null) {
					revisionSeqId = revId;
					// LOG.info("***88888*****");
				} else {
					revisionSeqId = null;
					// LOG.info("***9999999*****");
					break;
				}

			}

		}
		return null;
	}

	// added by santosh end on 19
	/**
	 * @param articleSeqId
	 * @return java.util.List<FolderMetaData>
	 * @roseuid 4940D66F01EA
	 */
	public List getHistoryList(String articleSeqId, String userId)
			throws DRBCommonException {

		String query = null;
		List accessLst = null;

		Object obj[] = new Object[] { articleSeqId };
		query = DRBQueryConstants.SHOW_ARTICLE_HISTORY_DATA;
		accessLst = getSimpleJdbcTemplate().query(query, historyMapper, obj);
		return accessLst;
	}

	/**
	 * @param articleSeqId
	 * @return com.geinfra.gedrb.data.ArticleData
	 */
	public List<ArticleData> toDisplayAlert(String articleSeqId)
			throws DRBCommonException {

		String query = DRBQueryConstants.VIEW_ARTICLE_DATA;
		List<ArticleData> articleDataList = null;
		articleDataList = getSimpleJdbcTemplate().query(query, articleMapper,
				new Object[] { articleSeqId });
		return articleDataList;
	}

	// Added by khurshid imam for current version link
	/**
	 * This method gets the current or latest version of the article
	 * articleSeqId is passed and queried to get the article data whose revision
	 * sequence id is same as articleSeqId of the passed method. The result
	 * entry is to be displayed as the latest version of article.
	 * 
	 * @param articleSeqId
	 * @return articleSeqId of the latest version article
	 */
	public String getArticleForCurentVersion(String articleSeqId)
			throws DRBCommonException {
		String articleSeqIdOfLatestVersion = "";
		String articleCode = "";
		// Get article code
		articleCode = (String) getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.GET_ARTICLE_CODE,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						String artCode = rs.getString("ARTICLE_CODE");
						return artCode;
					}
				}, articleSeqId);
		// Get ArtSeqId of latest article
		String query = " select ARTICLE_SEQ_ID from ( SELECT CAWC.DRB_ARTICLE.*, level ln FROM CAWC.DRB_ARTICLE CONNECT BY PRIOR"
				+ " ARTICLE_SEQ_ID=REVISION_ARTICLE_SEQ_ID START WITH ARTICLE_SEQ_ID=(SELECT ARTICLE_SEQ_ID "
				+ " FROM CAWC.DRB_ARTICLE WHERE ARTICLE_CODE = '"
				+ articleCode
				+ "') order by level desc ) where rownum = 1 "
				+ "AND ARTICLE_WF_STATE_SEQ_ID != 2";

		int artseqid = 0;
		try {
			artseqid = (getSimpleJdbcTemplate().queryForInt(query,
					new Object[] {}));
		} catch (Exception ex) {
			LOG.info("Exception caught in getArticleForCurentVersion");
			artseqid = Integer.parseInt(articleSeqId);
		}
		articleSeqIdOfLatestVersion = String.valueOf(artseqid);
		return articleSeqIdOfLatestVersion;
	}

	// End of addition by khurshid imam for current version link

	// Added by khurshid imam for DRB Archival
	public String getRevisionArticleSeqId(String articleSeqId) {
		String revsionArticleSeqId = "";
		// Get revsionArticleSeqId
		try {
			revsionArticleSeqId = (String) getSimpleJdbcTemplate()
					.queryForObject(DRBQueryConstants.GET_REV_ARTICLE_SEQ_ID,
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									String artCode = rs
											.getString("REVISION_ARTICLE_SEQ_ID");
									return artCode;
								}
							}, articleSeqId);
			LOG.info("Revision ArticleSeqId returned : " + revsionArticleSeqId);
		} catch (Exception ex) {
			revsionArticleSeqId = "";
		}
		return revsionArticleSeqId;
	}

	public List<String> getVerificationScoreForRevision(String articleSeqId) {
		// Query String
		String DOC_SEQ_AND_VERIFICATION_SCORE = "select DOCUMENT_SEQ_ID, document_property_value from CAWC.drb_document_property where "
				+ " document_seq_id in (select DOCUMENT_SEQ_ID from CAWC.DRB_DOCUMENT where APP_DATA_PK_ID = ? ) and "
				+ " document_property_name = 'VERIFICATION_SCORE'";

		List<String> docSeqIdVerificationScore = getSimpleJdbcTemplate().query(
				DOC_SEQ_AND_VERIFICATION_SCORE,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						String docSeqId = rs.getString("DOCUMENT_SEQ_ID");
						String propValue = "";
						if (rs.getString("document_property_value") == null) {
							propValue = "";
						} else {
							propValue = rs.getString("document_property_value");
						}
						return docSeqId + "#" + propValue;
					}
				}, articleSeqId);

		return docSeqIdVerificationScore;
	}

	public String getDocSeqIdForRevision(String docSeqId, String articleSeqId,
			String newArticleSeqId) {
		String docSeqIdForRevision = "";
		try {
			String query = "select J.DOCUMENT_SEQ_ID from CAWC.DRB_DOCUMENT J, CAWC.DRB_DOCUMENT B where J.document_name = "
					+ " B.document_name AND B.DOCUMENT_SEQ_ID = ? and J.DOCUMENT_SEQ_ID != ? and B.app_data_pk_id = ?  AND J.app_data_pk_id = ?";

			docSeqIdForRevision = (String) getSimpleJdbcTemplate()
					.queryForObject(
							query,
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int arg1)
										throws SQLException {
									return rs.getString("DOCUMENT_SEQ_ID");
								}
							}, docSeqId, docSeqId, articleSeqId,
							newArticleSeqId);
		} catch (Exception ex) {
			LOG.info(ex.getMessage());
			docSeqIdForRevision = "";
		}
		return docSeqIdForRevision;
	}

	public boolean insertScoreForRevision(String docSeqId,
			String verificationscore, String userId) {
		boolean isUpdated = false;
		try {
			String query = "INSERT INTO CAWC.DRB_DOCUMENT_PROPERTY (DOCUMENT_PROPERTY_SEQ_ID, CREATION_DATE,CREATED_BY,"
					+ " LAST_UPDATE_DATE, LAST_UPDATED_BY, DOCUMENT_PROPERTY_NAME, DOCUMENT_PROPERTY_VALUE, DOCUMENT_SEQ_ID) "
					+ " VALUES (CAWC.DRB_DOCUMENT_PROPERTY_SEQ.NEXTVAL,SYSDATE, ?,SYSDATE,"
					+ " ?,'VERIFICATION_SCORE', ?, ?)";

			int count = 0;
			count = getSimpleJdbcTemplate().update(query, userId, userId,
					verificationscore, docSeqId);
			if (count != 0) {
				isUpdated = true;
			}
		} catch (Exception ex) {
			LOG.info(ex.getMessage());
			LOG.info("xception caught in insertScoreForRevision");
		}

		return isUpdated;

	}

	public String getAttachmentVerificationScore(int attachSeqId) {
		String attachVerificationScore = null;
		try {
			// Get article code
			attachVerificationScore = (String) getSimpleJdbcTemplate()
					.queryForObject(
							DRBQueryConstants.GET_DOCUMENT_VERIFICATION_SCORE,
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									if (rs.getString("DOCUMENT_PROPERTY_VALUE") == null) {
										return null;
									} else {
										String artCode = rs
												.getString("DOCUMENT_PROPERTY_VALUE");
										return artCode;
									}
								}
							}, String.valueOf(attachSeqId));
		} catch (Exception ex) {
			attachVerificationScore = null;
		} finally {
			if (attachVerificationScore == null) {
				attachVerificationScore = "";
			}
		}
		return attachVerificationScore;
	}

	public boolean checkIfArticleVersionExists(String articleSeqId) {
		boolean articleVersionExists = false;
		int noOfRows = 0;
		try {
			noOfRows = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.CHECK_IF_ARTICLE_VERSION_EXISTS,
					articleSeqId, articleSeqId);
			if (noOfRows > 0) {
				articleVersionExists = true;
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught while checking for article revision for articleSeqId: "
					+ articleSeqId);
			LOG.info("Error is: " + ex.getMessage());
		}
		return articleVersionExists;
	}

	public boolean updateVerificationScore(String verificationScore,
			String attachSeqId, String userID) {
		int count = 0;
		boolean isUpdated = false;
		try {
			count = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_DOCUMENT_VERIFICATION_SCORE,
					new Object[] { userID, verificationScore, attachSeqId });
			if (count != 0) {
				isUpdated = true;
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught while UPDATING verificationScore for attachSeqId: "
					+ attachSeqId);
			LOG.info("Error is: " + ex.getMessage());
		}
		return isUpdated;
	}

	// following method is added by dhiraj for bug 1926
	public void insertCorruptFileExt(List<String> fileExtList, String userId) {

		try {
			if (!DRBUtils.isEmptyList(fileExtList)) {
				for (int i = 0; i < fileExtList.size(); i++) {
					String theCntQry = "SELECT COUNT(1) cnt FROM CAWC.DRB_DOC_exclusion_File_Ext WHERE  doc_File_Ext_Type = ?";
					int count = getSimpleJdbcTemplate().queryForInt(theCntQry,
							fileExtList.get(i));
					if (count == 0) {
						String theInsertQry = "INSERT INTO CAWC.DRB_DOC_exclusion_File_Ext (doc_exclusion_File_Ext_seq_ID,"
								+ " doc_File_Ext_Type, "
								+ "creation_date, created_by,last_update_date,last_updated_by) VALUES "
								+ "(CAWC.DRB_DOC_exclusion_File_Ext_SEQ.NEXTVAL,?,SYSDATE,user, SYSDATE,user)";
						getSimpleJdbcTemplate().update(theInsertQry,
								new Object[] { fileExtList.get(i) });
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean updateFileSize(String fileSize, String attachSeqId,
			String userID) {
		int count = 0;
		boolean isUpdated = false;
		try {
			count = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_DOCUMENT_FILE_SIZE,
					new Object[] { userID, fileSize, attachSeqId });
			if (count != 0) {
				isUpdated = true;
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught while UPDATING fileSize for attachSeqId: "
					+ attachSeqId);
			LOG.info("Error is: " + ex.getMessage());
		}
		return isUpdated;
	}

	public boolean updateAttachmentForReplace(String fileName,
			String attachSeqId, String articleseqId, String userId,
			String fileReplacedPath) {
		int count = 0;
		int locationIDCount;
		int locationID;
		boolean isUpdated = false;
		try {
			locationIDCount = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.GET_LOCATION_SEQ_COUNT, fileReplacedPath);

			if (locationIDCount == 0) {
				locationID = getSimpleJdbcTemplate().queryForInt(
						DRBQueryConstants.GET_NEW_LOCATION_SEQ);
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.INSERT_DOCUMENT_LOCATION, locationID,
						fileReplacedPath, userId, userId);

			} else {
				locationID = getSimpleJdbcTemplate().queryForInt(
						DRBQueryConstants.GET_LOCATION_SEQ, fileReplacedPath);
			}

			count = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_DOCUMENT_FOR_REPLACE,
					new Object[] { fileName,
							fileName.substring(fileName.indexOf('~') + 1),
							locationID, userId, attachSeqId, articleseqId });
			if (count != 0) {
				isUpdated = true;
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught while Replacing attachement attachSeqId: "
					+ attachSeqId);
			LOG.info("Error is: " + ex.getMessage());
		}
		return isUpdated;
	}

	public String taskExistsForArticleAttachment(String articleseqId) {
		String activeInd = "";
		try {
			activeInd = (String) getSimpleJdbcTemplate().queryForObject(
					DRBQueryConstants.TASK_EXISTS_FOR_ARTICLE_ATTACHMENT,
					new ParameterizedRowMapper<String>() {
						public String mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							String artCode = rs.getString("ACTIVE_IND");
							return new String(artCode);
						}
					}, articleseqId);

			if (activeInd != null && !activeInd.equalsIgnoreCase("N")
					&& !activeInd.equalsIgnoreCase("Y")) {
				activeInd = ArticleConstants.ACTIVE_IND_NULL;
			}
		} catch (Exception ex) {
			activeInd = ArticleConstants.ACTIVE_IND_NULL;
		}
		return activeInd;
	}

	public String taskExistsForArticleReviewer(String articleseqId) {
		int count = 0;
		String activeInd = "";
		try {
			count = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.GET_TASK_REVIEWER, articleseqId,
					ArticleConstants.STRING_DRB_ARTICLE,
					ArticleConstants.ARTICLE_REQUEST_TYPE_NAME,
					ArticleConstants.STRING_PENDING);

			if (count > 0) {
				activeInd = ArticleConstants.CHAR_Y;
			}
		} catch (Exception ex) {
			activeInd = ArticleConstants.CHAR_Y;
		}
		return activeInd;
	}

	public boolean insertTaskForArticleAttachment(String articleseqId,
			String articleState, String userId) {
		int count = 0;
		boolean isUpdated = false;
		String articleCode = "";
		try {
			articleCode = (String) getSimpleJdbcTemplate().queryForObject(
					DRBQueryConstants.GET_ARTICLE_CODE,
					new ParameterizedRowMapper<String>() {
						public String mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							String artCode = rs.getString("ARTICLE_CODE");
							return new String(artCode);
						}
					}, articleseqId);

			String query = "SELECT WF_STATE_SEQ_ID FROM CAWC.DRB_WF_STATE WHERE STATE_NAME = '"
					+ articleState + "'";

			int request_wf_state_seq_id = (getSimpleJdbcTemplate().queryForInt(
					query, new Object[] {}));

			String INSERT_TASK_FOR_ARTICLE_ATTACHMENT = "INSERT INTO CAWC.DRB_REQUEST_EVENT "
					+ " (request_event_seq_id, App_data_pk_id, App_table_seq_id, request_type_id,active_ind,"
					+ " request_wf_state_seq_id, requestor_user_id, recipient_user_id,request_date, request_description,"
					+ " creation_date, created_by, last_update_date, last_updated_by) "
					+ " values (CAWC.DRB_REQUEST_EVENT_SEQ.nextval, ?, (SELECT APP_TABLE_SEQ_ID FROM "
					+ "CAWC.DRB_APP_TABLE WHERE APP_TABLE_NAME = 'DRB_ARTICLE'), "
					+ " (SELECT REQUEST_TYPE_ID FROM CAWC.DRB_REQUEST_TYPE "
					+ "WHERE REQUEST_TYPE_NAME = '"
					+ ArticleConstants.ATTACHMENT_TASK_TYPE
					+ "'),"
					+ " 'Y',"
					+ " ?, ?, ?, "
					+ " sysdate, '"
					+ ArticleConstants.ATTACHMENT_TASK_DESCRIPTION
					+ articleCode + "' , sysdate, ?, sysdate,?)";

			count = getSimpleJdbcTemplate().update(
					INSERT_TASK_FOR_ARTICLE_ATTACHMENT, articleseqId,
					request_wf_state_seq_id, userId, userId, userId, userId);
			if (count != 0) {
				isUpdated = true;
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught in insertTaskForArticleAttachment articleseqId: "
					+ articleseqId);
			LOG.info("Error is: " + ex.getMessage());
		}
		return isUpdated;
	}

	public boolean updateAttachmentTask(String articleseqId, String userId,
			String activeInd) {
		int count = 0;
		boolean isUpdated = false;
		try {
			count = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_TASK_FOR_ARTICLE_ATTACHMENT,
					activeInd, userId, articleseqId);
			if (count != 0) {
				isUpdated = true;
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught in updateAttachmentTask articleseqId: "
					+ articleseqId);
			LOG.info("Error is: " + ex.getMessage());
		}
		return isUpdated;
	}

	public boolean updateReviewerMetadata(String articleseqId, String userId) {
		int count = 0;
		boolean isUpdated = false;
		try {
			count = getSimpleJdbcTemplate()
					.update(DRBQueryConstants.UPDATE_REVIEWER_METADATA_IND_FOR_ATTACHMENT,
							userId, articleseqId);
			if (count != 0) {
				isUpdated = true;
			}
		} catch (Exception ex) {
			LOG.info("Exeption caught in updateReviewerMetadata articleseqId: "
					+ articleseqId);
			LOG.info("Error is: " + ex.getMessage());
		}
		return isUpdated;
	}

	public void resubmitIssuedArticleForReview(ArticleData articleData,
			String userFullName, String userId) {
		try {
			// Remove reviewer approvals & Return article to In Review state
			// Resubmit article for review.Send e-mail notice to reviewer(s)
			articleData.setOldValue("Issued");
			articleData.setNewValue("In Review");
			articleData.setArticleSeqId(Integer.parseInt(articleData
					.getArticleSeqid()));
			articleData
					.setChangedAttributeName("Resubmit Issued Article For Review");
			this.captureArticlesHistory(articleData);

			getSimpleJdbcTemplate().update(
					DRBQueryConstants.RESUBMIT_TO_REVIEW_FOR_ISSUED_ARTICLE,
					"NONE", "NONE", "NONE", ArticleConstants.CHAR_N, "",
					ArticleConstants.STATE_IN_REVIEW,
					ArticleConstants.STATE_IN_REVIEW, userFullName,
					articleData.getArticleSeqid());

			String taskDesc = "Article#: " + articleData.getArticleNumber()
					+ " assigned for Review as Primary Reviewer";
			if (articleData.getReviewerid() != null
					&& !articleData.getReviewerid().equals("")) {
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.ASSIGN_REVIEWER_REQUEST_EVENT,
								new Object[] {
										Integer.parseInt(articleData
												.getArticleSeqid()), userId,
										articleData.getReviewerid(), taskDesc,
										articleData.getLibrarySeqId(),
										userFullName, userFullName });
			}

			String taskDesc1 = "Article#: " + articleData.getArticleNumber()
					+ " assigned for Review as Additional Reviewer";

			if (articleData.getAdditionalReviewerId() != null
					&& !articleData.getAdditionalReviewerId().equals("")) {
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.ADD_ADDITIONAL_REVIEWER_REQUEST_EVENT,
								new Object[] {
										Integer.parseInt(articleData
												.getArticleSeqid()), userId,
										articleData.getAdditionalReviewerId(),
										taskDesc1,
										articleData.getLibrarySeqId(),
										userFullName, userFullName });
			}

			// End of code added by dhiraj

		} catch (Exception ex) {
			LOG.info("Exeption caught in resubmitIssuedArticleForReview articleseqId: "
					+ articleData.getArticleSeqid());
			LOG.info("Error is: " + ex.getMessage());
		}

	}

	public void resubmitReviewedArticleForReview(ArticleData articleData,
			String userFullName, String userId) {
		try {
			// Remove reviewer approvals & Return article to In Review state
			// Resubmit article for review.Send e-mail notice to reviewer(s)
			articleData.setOldValue("In Review");
			articleData.setNewValue("In Review");
			articleData.setArticleSeqId(Integer.parseInt(articleData
					.getArticleSeqid()));
			articleData
					.setChangedAttributeName("Resubmit Reviewed Article For Review");
			this.captureArticlesHistory(articleData);

			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.RESUBMIT_TO_APPROVEEDITS_FOR_REVIEW_ARTICLE,
							"NONE", "NONE", "NONE", ArticleConstants.CHAR_N,
							ArticleConstants.STATE_IN_REVIEW, userFullName,
							articleData.getArticleSeqid());

			String taskDesc = "Article#: " + articleData.getArticleNumber()
					+ " assigned for Review as Primary Reviewer";
			if (articleData.getReviewerid() != null
					&& !articleData.getReviewerid().equals("")) {

				int count = getSimpleJdbcTemplate()
						.queryForInt(
								DRBQueryConstants.GET_TASK_REVIEW_FOR_ATTACHMENT_VERIFICATION,
								Integer.parseInt(articleData.getArticleSeqid()),
								articleData.getReviewerid());
				if (count == 0) {
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.ASSIGN_REVIEWER_REQUEST_EVENT,
							new Object[] {
									Integer.parseInt(articleData
											.getArticleSeqid()), userId,
									articleData.getReviewerid(), taskDesc,
									articleData.getLibrarySeqId(),
									userFullName, userFullName });
				}
			}

			String taskDesc1 = "Article#: " + articleData.getArticleNumber()
					+ " assigned for Review as Additional Reviewer";

			if (articleData.getAdditionalReviewerId() != null
					&& !articleData.getAdditionalReviewerId().equals("")) {
				int count = getSimpleJdbcTemplate()
						.queryForInt(
								DRBQueryConstants.GET_TASK_REVIEW_FOR_ATTACHMENT_VERIFICATION,
								Integer.parseInt(articleData.getArticleSeqid()),
								articleData.getAdditionalReviewerId());
				if (count == 0) {
					getSimpleJdbcTemplate()
							.update(DRBQueryConstants.ADD_ADDITIONAL_REVIEWER_REQUEST_EVENT,
									new Object[] {
											Integer.parseInt(articleData
													.getArticleSeqid()),
											userId,
											articleData
													.getAdditionalReviewerId(),
											taskDesc1,
											articleData.getLibrarySeqId(),
											userFullName, userFullName });
				}
			}

			// End of code added by dhiraj

			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.DELETE_REQUESTEVENT_ARTICLE_APPROVAL_REVIEW_ARTICLE,
							ArticleConstants.ARTICLE_APPROVAL,
							articleData.getArticleSeqid(),
							ArticleConstants.STRING_DRB_ARTICLE);

		} catch (Exception ex) {
			LOG.info("Exeption caught in resubmitReviewedArticleForReview articleseqId: "
					+ articleData.getArticleSeqid());
			LOG.info("Error is: " + ex.getMessage());
		}

	}

	public void resubmitReviewedArticleForReview(ArticleData articleData,
			String userFullName) {
		try {
			// Remove reviewer approvals & Return article to In Review state
			// Resubmit article for review.Send e-mail notice to reviewer(s)
			articleData.setOldValue("In Review");
			articleData.setNewValue("In Review");
			articleData.setArticleSeqId(Integer.parseInt(articleData
					.getArticleSeqid()));
			articleData
					.setChangedAttributeName("Resubmit Reviewed Article For Review");
			this.captureArticlesHistory(articleData);

			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.RESUBMIT_TO_APPROVEEDITS_FOR_REVIEW_ARTICLE,
							"NONE", "NONE", "NONE", ArticleConstants.CHAR_N,
							ArticleConstants.STATE_IN_REVIEW, userFullName,
							articleData.getArticleSeqid());

			UserData ownerUserObj = (UserData) (DRBUtils
					.getServletSession(false)
					.getAttribute(ArticleConstants.SESSION_USER_DATA));

			String taskDesc = "Article#: " + articleData.getArticleNumber()
					+ " assigned for Review as Primary Reviewer";
			if (articleData.getReviewerid() != null
					&& !articleData.getReviewerid().equals("")) {
				int count = getSimpleJdbcTemplate()
						.queryForInt(
								DRBQueryConstants.GET_TASK_REVIEW_FOR_ATTACHMENT_VERIFICATION,
								Integer.parseInt(articleData.getArticleSeqid()),
								articleData.getReviewerid());
				if (count == 0) {
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.ASSIGN_REVIEWER_REQUEST_EVENT,
							new Object[] {
									Integer.parseInt(articleData
											.getArticleSeqid()),
									ownerUserObj.getUserSSOID(),
									articleData.getReviewerid(), taskDesc,
									articleData.getLibrarySeqId(),
									userFullName, userFullName });
				}
			}

			String taskDesc1 = "Article#: " + articleData.getArticleNumber()
					+ " assigned for Review as Additional Reviewer";

			if (articleData.getAdditionalReviewerId() != null
					&& !articleData.getAdditionalReviewerId().equals("")) {
				int count = getSimpleJdbcTemplate()
						.queryForInt(
								DRBQueryConstants.GET_TASK_REVIEW_FOR_ATTACHMENT_VERIFICATION,
								Integer.parseInt(articleData.getArticleSeqid()),
								articleData.getAdditionalReviewerId());
				if (count == 0) {
					getSimpleJdbcTemplate()
							.update(DRBQueryConstants.ADD_ADDITIONAL_REVIEWER_REQUEST_EVENT,
									new Object[] {
											Integer.parseInt(articleData
													.getArticleSeqid()),
											ownerUserObj.getUserSSOID(),
											articleData
													.getAdditionalReviewerId(),
											taskDesc1,
											articleData.getLibrarySeqId(),
											userFullName, userFullName });
				}
			}

			// End of code added by dhiraj

		} catch (Exception ex) {
			LOG.info("Exeption caught in resubmitReviewedArticleForReview articleseqId: "
					+ articleData.getArticleSeqid());
			LOG.info("Error is: " + ex.getMessage());
		}

	}

	public void resubmitIssuedArticleToDraft(ArticleData articleData,
			String userFullName, String userId) {
		try {
			// Return article to draft state
			articleData.setOldValue("Issued");
			articleData.setNewValue("Draft");
			articleData.setArticleSeqId(Integer.parseInt(articleData
					.getArticleSeqid()));
			articleData
					.setChangedAttributeName("Resubmit Issued Article to Draft");
			this.captureArticlesHistory(articleData);

			getSimpleJdbcTemplate().update(
					DRBQueryConstants.RESUBMIT_TO_DRAFT_FOR_ISSUED_ARTICLE,
					ArticleConstants.STATE_DRAFT, userFullName,
					articleData.getArticleSeqid());
		} catch (Exception ex) {
			LOG.info("Exeption caught in resubmitIssuedArticleToDraft articleseqId: "
					+ articleData.getArticleSeqid());
			LOG.info("Error is: " + ex.getMessage());
		}

	}

	public void reIssueArticle(ArticleData articleData, String userFullName,
			String userId) {
		try {
			// Re issue article
			articleData.setOldValue("Issued");
			articleData.setNewValue("Issued");
			articleData.setArticleSeqId(Integer.parseInt(articleData
					.getArticleSeqid()));
			articleData.setChangedAttributeName("Reissue article");
			this.captureIssueArticlesHistory(articleData);

			getSimpleJdbcTemplate().update(
					DRBQueryConstants.REISSUE_FOR_ISSUED_ARTICLE, userFullName,
					articleData.getArticleSeqid());

		} catch (Exception ex) {
			LOG.info("Exeption caught in reIssueArticle articleseqId: "
					+ articleData.getArticleSeqid());
			LOG.info("Error is: " + ex.getMessage());
		}

	}

	// End of addition by khurshid imam for DRB Archival

	/**
	 * @return the moveArticlePrc
	 */
	public MoveArticleProcedure getMoveArticlePrc() {
		return _moveArticlePrc;
	}

	/**
	 * @param moveArticlePrc
	 *            the moveArticlePrc to set
	 */
	public void setMoveArticlePrc(MoveArticleProcedure moveArticlePrc) {
		this._moveArticlePrc = moveArticlePrc;
	}

	/**
	 * @param articleSeqID
	 * @return List<ArticleAmendData>
	 * @throws DRBCommonException
	 */
	public List<ArticleAmendData> fetchAttachmentDetails(String articleSeqID)
			throws DRBCommonException {
		// Log.info("In DAO --------------" + articleSeqID);
		List<ArticleAmendData> attachmentDetailList = null;

		String fetchAttachmentsQuery = DRBQueryConstants.SEL_ATTACHMENT_LIST;
		// Log.info("In DAO --------------" + fetchAttachmentsQuery);
		try {
			attachmentDetailList = null;
			attachmentDetailList = getSimpleJdbcTemplate().query(
					fetchAttachmentsQuery, attachmentMapper, articleSeqID);
			// Log.info("Generated List -----" + attachmentDetailList);
		} catch (Exception e) {
			e.getMessage();
		}
		return attachmentDetailList;

	}

	public void deleteAttachments(String articleSeqID)
			throws DRBCommonException {
		try {
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.DEL_ATTACHMENT_PROPERTY,
					new Object[] { articleSeqID });
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.DEL_ATTACHMENT_DOCUMENT,
					new Object[] { articleSeqID });
			// Log.info("Generated List -----" + attachmentDetailList);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * This method is used to get the files uplaoded
	 * 
	 * @param docSeqId
	 *            , userID
	 * @return java.util.ArrayList
	 * @roseuid 48D0A641014B
	 */
	@Transactional
	public FileData viewUploadedFile(String docSeqId, String userID,
			String loggedInUserName) throws DRBCommonException {
		FileData fileData = null;
		StringBuffer selectFileQuery = new StringBuffer();
		StringBuffer userViewedQuery = new StringBuffer();
		List<MyArticlesData> userViewed = null;
		int count = 0;
		int docID = 0;

		selectFileQuery.append(DRBQueryConstants.SELECT_UPLOADED_FILE);
		userViewedQuery.append(DRBQueryConstants.SELECT_ASSET_IN_USER_VIEWED);

		userViewed = getSimpleJdbcTemplate().query(userViewedQuery.toString(),
				userViewedMapper, docSeqId, userID);
		for (Iterator<MyArticlesData> i = userViewed.iterator(); i.hasNext();) {
			MyArticlesData articleObj = i.next();
			count = articleObj.getCount();
			docID = articleObj.getDocSeqId();
		}
		// Log.info("User ID ------>" + userID + "" + docSeqId);
		// Log.info("Count of UserViewed ------>" + userViewed);
		// Log.info("User Viewed Query ------------>" + userViewedQuery);
		if (count > 0) {
			// Log.info("Need to update Record");
			String updateExistingQuery = DRBQueryConstants.UPDATE_USER_VIEWED_ASSET;
			int updateResult = getSimpleJdbcTemplate().update(
					updateExistingQuery, loggedInUserName, docSeqId, docID,
					userID);
			if (updateResult > 0) {
				fileData = (FileData) getSimpleJdbcTemplate().queryForObject(
						selectFileQuery.toString(), fileDataMapper, docSeqId);
			} else {
				// Log.info("Could not update table");
			}
		} else {
			// Log.info("Need insert new Record");
			String insertData = DRBQueryConstants.INSERT_NEW_ENTRY;
			int insertionResult = getSimpleJdbcTemplate().update(insertData,
					docSeqId, userID, loggedInUserName, loggedInUserName);
			if (insertionResult > 0) {
				fileData = (FileData) getSimpleJdbcTemplate().queryForObject(
						selectFileQuery.toString(), fileDataMapper, docSeqId);
			} else {
				// Log.info("Could not insert into table");
			}
		}

		// //Log.info("After Method in DAO");
		selectFileQuery = null;// heap issue
		selectFileQuery = null;// heap issue
		return fileData;
	}

	/**
	 * @param docSeqId
	 * @param userID
	 * @throws DRBCommonException
	 */
	@Transactional
	public void modifyDetails(String docSeqId, String userID,
			String loggedInUserName) throws DRBCommonException {
		List<MyArticlesData> userViewed = null;
		StringBuffer userViewedQuery = new StringBuffer();
		int count = 0;
		int docID = 0;
		userViewedQuery.append(DRBQueryConstants.SELECT_ASSET_IN_USER_VIEWED);

		userViewed = getSimpleJdbcTemplate().query(userViewedQuery.toString(),
				userViewedMapper, docSeqId, userID);
		for (Iterator<MyArticlesData> i = userViewed.iterator(); i.hasNext();) {
			MyArticlesData articleObj = i.next();
			count = articleObj.getCount();
			docID = articleObj.getDocSeqId();
			// Log.info("COUNT " + articleObj.getCount());
			// Log.info("Document Seq ID " + articleObj.getDocSeqId());
		}
		// Log.info("User ID ------>" + userID + "" + docSeqId);
		// Log.info("Count of UserViewed ------>" + count);
		// Log.info("User Viewed Query ------------>" + userViewedQuery);
		if (count > 0) {
			StringBuffer updateExistingQuery = new StringBuffer();
			updateExistingQuery
					.append(DRBQueryConstants.UPDATE_USER_VIEWED_ASSET);
			getSimpleJdbcTemplate().update(updateExistingQuery.toString(),
					loggedInUserName, docSeqId, docID, userID);
			updateExistingQuery = null;// heap issue
		} else {
			// Log.info("Need insert new Record");
			StringBuffer insertData = new StringBuffer();
			insertData.append(DRBQueryConstants.INSERT_NEW_ENTRY);
			// insertionResult never used any where //Code Review Changed By
			// Naresh on 22DEC10
			getSimpleJdbcTemplate().update(insertData.toString(), docSeqId,
					userID, loggedInUserName, loggedInUserName);
			insertData = null;// heap issue
		}
		userViewedQuery = null;// herap issue
	}

	public FileData viewUploadedNAFile(String docSeqId)
			throws DRBCommonException {
		FileData fileData = null;
		StringBuffer selectFileQuery = new StringBuffer();
		selectFileQuery.append(DRBQueryConstants.SELECT_UPLOADED_FILE_DOWNLOAD);

		fileData = (FileData) getSimpleJdbcTemplate().queryForObject(
				selectFileQuery.toString(), fileDataDownloadMapper, docSeqId);

		selectFileQuery = null;// heap issue
		return fileData;
	}

	/**
	 * @param attachmentSeqIDList
	 * @return
	 * @throws DRBCommonException
	 */
	public List<FileData> bulkDownloadFiles(List attachmentSeqIDList)
			throws DRBCommonException {
		List<FileData> fileData = null;
		String fetchDocumentNameQuery = null;
		String attachmentIDs = null;
		attachmentIDs = attachmentSeqIDList.toString();
		// Log.info(attachmentIDs);
		attachmentIDs = attachmentIDs.substring(1, attachmentIDs.length() - 1);
		// Log.info(attachmentIDs);
		fetchDocumentNameQuery = DRBQueryConstants.FETCH_DOCUMENT_NAME
				+ attachmentIDs + ")";
		fileData = getSimpleJdbcTemplate().query(fetchDocumentNameQuery,
				fileDownloadMapper);
		return fileData;
	}

	/**
	 * @param ids
	 * @return List<Map<String, String>>
	 */
	public List<Map<String, String>> getRequestorOwnerDetails(String... ids) {
		String queryForReqOwnerDetails = "select uname,givenname,familyname,realemail from CAWC.drb_user where uname in(?,?)";
		List<Map<String, String>> userDetails = getSimpleJdbcTemplate().query(
				queryForReqOwnerDetails,
				new ParameterizedRowMapper<Map<String, String>>() {
					public Map<String, String> mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						Map<String, String> userDetailsMap = new HashMap<String, String>();
						userDetailsMap.put("ssoID", rs.getString("UNAME"));
						// Log.info(rs.getString("UNAME"));
						userDetailsMap.put("givenName",
								rs.getString("GIVENNAME"));
						// Log.info(rs.getString("GIVENNAME"));
						userDetailsMap.put("familyName",
								rs.getString("FAMILYNAME"));
						// Log.info(rs.getString("FAMILYNAME"));
						userDetailsMap.put("realEmail",
								rs.getString("REALEMAIL"));
						// Log.info(rs.getString("REALEMAIL"));
						return userDetailsMap;
					}
				}, (Object[]) ids);
		return userDetails;
	}

	/**
	 * @param userGroupID
	 * @return List<String>
	 */
	public List<String> getUsersOfTheGroup(String userGroupID) {

		// Query String
		String USERS_OF_A_GROUP_BY_GROUP_ID = DRBQueryConstants.NTK_USERINGROUP;

		List<String> ssoOfGroupUsers = getSimpleJdbcTemplate().query(
				USERS_OF_A_GROUP_BY_GROUP_ID,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						String ssoIDOfAMember = rs.getString("USER_ID");

						return ssoIDOfAMember;
					}
				}, userGroupID);
		return ssoOfGroupUsers;
	}

	/**
	 * @param linID
	 * @return String
	 */
	public String getECTInForce(String linID) {
		String ECT_TAG = DRBQueryConstants.NTK_ECTINFORCE;

		List<String> ssoOfGroupUsers = getSimpleJdbcTemplate().query(ECT_TAG,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						String ectTag = rs.getString("ECT_ENFORCED_IND") + "~"
								+ rs.getString("EXPORT_CLASS_NAME");

						return ectTag;
					}
				}, linID);

		return ssoOfGroupUsers.get(0);
	}

	/**
	 * @param linID
	 * @return String
	 */
	public String getArticleGrantAccessInd(String linID) {
		String GRT_ACC_IND = DRBQueryConstants.NTK_ARTICLE_GRANT_ACCESS_IND;

		List<String> ssoOfGroupUsers = getSimpleJdbcTemplate().query(
				GRT_ACC_IND, new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						String ectTag = rs
								.getString("ARTICLE_GRANT_ACCESS_IND");

						return ectTag;
					}
				}, linID);

		return ssoOfGroupUsers.get(0);
	}

	/**
	 * @param articleFolderId
	 * @param type
	 * @return String
	 */
	public String getECTInForceForArticleFolders(String articleFolderId,
			String type) {
		// Log.info("article/folder type>>>>>>>>>>>>>>>>>>>" + type);
		if (type.equalsIgnoreCase("Article")) {
			StringBuffer ECT_TAG = new StringBuffer();
			ECT_TAG.append(DRBQueryConstants.NTK_ECTINFORCE_ARTICLE);
			List<String> ssoOfGroupUsers = getSimpleJdbcTemplate().query(
					ECT_TAG.toString(), new ParameterizedRowMapper<String>() {
						public String mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							String ectTag = rs.getString("EXPORT_CLASS_NAME");
							return ectTag;
						}
					}, articleFolderId);
			ECT_TAG = null;// /heap issue
			return ssoOfGroupUsers.get(0);
		} else {
			StringBuffer ECT_TAG = new StringBuffer();
			ECT_TAG.append(DRBQueryConstants.NTK_ECTINFORCE_FODLER);
			List<String> ssoOfGroupUsers = null;
			ssoOfGroupUsers = new ArrayList<String>();
			ssoOfGroupUsers = getSimpleJdbcTemplate().query(ECT_TAG.toString(),
					new ParameterizedRowMapper<String>() {
						public String mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							String ectTag = rs.getString("EXPORT_CLASS_NAME");
							return ectTag;
						}
					}, articleFolderId);
			ECT_TAG = null;// /heap issue
			return ssoOfGroupUsers.get(0);
		}
	}

	public List<UserData> searchForTagger(String byName, String libId)
			throws DRBCommonException {
		List<UserData> listOfUsers = null;
		UserData toRemove = null;
		// Log.info(byName + ">>>>>>>>>>>>>>>" + libId);
		listOfUsers = _userServiceObj.getUsersInLibrary(byName, libId);
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		// //Log.info("userObj>>>>>>" + userObj + ">>>>>>>>>>>>" +
		// userObj.getUserSSOID());
		if (userObj != null) {
			for (UserData userList : listOfUsers) {
				if (userList.getUserSSOID().equals(userObj.getUserSSOID())) {
					toRemove = userList;
				}
			}
			if (toRemove != null) {
				listOfUsers.remove(toRemove);
			}
		}
		// Log.info("Size is listOfUsers" + listOfUsers.size());
		return listOfUsers;
	}

	public String getNetworkID(String sso) {
		StringBuffer altuserId = new StringBuffer();
		altuserId.append(DRBQueryConstants.NTK_NTWID);
		List<String> ntId = getSimpleJdbcTemplate().query(altuserId.toString(),
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("VALUE");
					}
				}, sso, DRBUtils.getUserPropertyName("USER_NT_LOGIN"));
		String networkId = "";
		if (ntId != null && ntId.size() > 0) {
			networkId = ntId.get(0);
		}
		// Log.info("In Dao >>>>>>>>>>>" + networkId);
		altuserId = null; // heap issue
		return networkId;
	}

	/**
	 * @return
	 */
	public CheckTaggingProcedure getCheckTaggingProcedure() {
		return _checkTaggingProcedure;
	}

	/**
	 * @param checkTaggingProcedure
	 */
	public void setCheckTaggingProcedure(
			CheckTaggingProcedure checkTaggingProcedure) {
		this._checkTaggingProcedure = checkTaggingProcedure;
	}

	/**
	 * This method is used to Cancel New Article and to Cancel Article Revision
	 * 
	 * @param artTitle
	 * @param articleSeqId
	 * @param articleCode
	 * @param notifyUserMailIds
	 * @return String
	 * @throws DRBCommonException
	 */
	public String cancelArticle(String ectvalue, String artTitle,
			String articleSeqId, String articleCode,
			List<String> notifyUserMailIds) throws DRBCommonException {
		String returnVal = null;
		String articleState = null;
		String revIndica = null;
		String revArtCode = null;
		// added by santosh on 6 dec
		// Log.info("cancelArticle() :: DAO :: article Sequence ID :::: " +
		// articleSeqId);
		// Log.info("cancelArticle() :: DAO :: article Code :::: " +
		// articleCode);

		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String userSSO = userObj.getUserSSOID();

		Map procRetVal = null;// new HashMap();
		procRetVal = _updateArticleStatePrc.excuteUpdateArticleStateProcedure(
				articleSeqId, ArticleConstants.ARTICLE_WF_ACTION_CANCEL
						.toUpperCase(Locale.US), userSSO);
		if (procRetVal != null)
			returnVal = (String) procRetVal.get(ArticleConstants.PROC_OUT_PUT);

		// Log.info("Cancel Article DAO :: Value returned from PROCEDURE :::: "
		// + returnVal);

		/*
		 * return value from procedure will be in the below format
		 * folderSeqId~revision Indicator~revision article Code which will move
		 * to issued state from Checked-out state i.e. folder123~Y~art123 [if
		 * revision Exists] i.e. folder123~N~NA [if NO revision Exists]
		 */

		if (returnVal != null) {
			String[] listToArray = null;// new String[3];
			listToArray = DRBUtils.split(returnVal, ArticleConstants.TILDA);
			if (listToArray != null && listToArray.length > 0) {
				returnVal = listToArray[0]; /* Folder Sequence ID */
				revIndica = listToArray[1]; /* Revision Indicator */
				revArtCode = listToArray[2]; /*
											 * Revision article code whcih
											 * changes its state from
											 * Checked-out to issued
											 */
				// Log.info("[revArtCode] after splitting ==>>> " + revArtCode);
			}
			// LOG.info("*******after query for delete article******");
			// added bybsantohs ends on 6 dec end
			/*
			 * if notification list is not empty then only send the notification
			 * email...
			 */
			if (notifyUserMailIds != null && notifyUserMailIds.size() > 0) {
				articleState = ArticleConstants.STATE_CANCELLED;
				sendStateChangeEmail(ectvalue, articleSeqId, artTitle,
						notifyUserMailIds, articleCode, revArtCode,
						articleState, revIndica,
						ArticleConstants.ARTICLE_WF_ACTION_CANCEL);
			}
		}
		// Log.info("Cancel Article DAO :: Value returned from DAO IMPL :::: " +
		// returnVal);
		return returnVal;
	}

	/**
	 * Used to Delete or Un-Delete an article
	 * 
	 * @param artTitle
	 * @param articleNo
	 * @param flag
	 * @return String
	 * @throws DRBCommonException
	 */
	public String deleteArticle(String ectValue, String artTitle,
			String articleSeqId, String articleCode, String delStateFlag,
			List<String> notifyUserMailIds) throws DRBCommonException {
		String returnVal = null;
		String articleState = null;
		String revIndica = null;
		// added by santosh on 29 dec for mail,when article deleted in Isuued
		// state start
		if (DRBUtils.getServletSession(false).getAttribute("USER_DATA") != null) {
			UserData userObj = (UserData) (DRBUtils.getServletSession(false)
					.getAttribute("USER_DATA"));
			notifyUserMailIds.add(userObj.getUserEMailID());
			// LOG.info("*******getUserEMailID******"+userObj.getUserEMailID());
		}
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String userSSO = userObj.getUserSSOID();
		// Log.info("Delete Article DAO :: User SSO ID :::: " + userSSO);

		Map procRetVal = null;// new HashMap();
		procRetVal = _updateArticleStatePrc.excuteUpdateArticleStateProcedure(
				articleSeqId, delStateFlag, userSSO);
		if (procRetVal != null) {
			returnVal = (String) procRetVal.get(ArticleConstants.PROC_OUT_PUT);
		}
		// Log.info("deleteArticle() :: Value returned from PROCEDURE :::: " +
		// returnVal);

		/*
		 * return value from procedure will be in the below format
		 * Folder/Article SeqId~revision Indicator i.e. folder123~Y [if revision
		 * Exists & action is Delete] i.e. folder123~N [if NO revision Exists &
		 * action is Delete] i.e. Article123~Y [if revision Exists & action is
		 * UnDelete] i.e. Article123~N [if NO revision Exists & action is
		 * UnDelete]
		 */

		if (returnVal != null) {
			String[] listToArray = null;// new String[2];
			listToArray = DRBUtils.split(returnVal, ArticleConstants.TILDA);
			if (listToArray != null && listToArray.length > 0) {
				returnVal = listToArray[0]; /* Folder/Article seq id */
				// Log.info("[returnVal] after splitting ==>>> " + returnVal);
				revIndica = listToArray[1];/* Revision Indicator */
				// Log.info("[revIndica] after splitting ==>>> " + revIndica);
			}

			/*
			 * if notification list is not empty then only send the notification
			 * email...
			 */
			String delStateFlagTemp = "";
			if (notifyUserMailIds != null && notifyUserMailIds.size() > 0) {
				if (delStateFlag != null
						&& delStateFlag
								.equalsIgnoreCase(ArticleConstants.EDRB_ACTON_FLAG02)) {
					articleState = ArticleConstants.STATE_ISSUED;
					delStateFlagTemp = delStateFlag.toUpperCase(Locale.US);
				} else if (delStateFlag != null
						&& delStateFlag
								.equalsIgnoreCase(ArticleConstants.EDRB_ACTON_FLAG01)) {
					articleState = ArticleConstants.STATE_DELETED;
					delStateFlagTemp = delStateFlag.toUpperCase(Locale.US);
				}
				sendStateChangeEmail(ectValue, articleSeqId, artTitle,
						notifyUserMailIds, articleCode, null, articleState,
						revIndica, delStateFlagTemp);
				// LOG.info("*********notifyUserMailIds in daoimpl444444********");
			}
		}

		// Log.info("Delete Article DAO :: Value returned from DAO IMPL :::: " +
		// returnVal);
		return returnVal;
	}

	/**
	 * This method is used to Demote An Article from Review
	 * 
	 * @param artTitle
	 * @param articleSeqId
	 * @param articleCode
	 * @param notifyUserMailIds
	 * @param demotionReason
	 * @return String
	 * @throws DRBCommonException
	 */
	public String demoteArticle(String ectValue, String artTitle,
			String articleSeqId, String articleCode,
			List<String> notifyUserMailIds, String demotionReason)
			throws DRBCommonException {
		String returnVal = null;

		// Log.info("demoteArticle() :: DAO :: article Sequence ID :::: " +
		// articleSeqId);
		// Log.info("demoteArticle() :: DAO :: article Code :::: " +
		// articleCode);
		// Log.info("Demote Article DAO :: Demotion Reason :::: " +
		// demotionReason);

		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String userSSO = userObj.getUserSSOID();
		String userName = userObj.getUserFirstName()
				+ ArticleConstants.EMPTY_WITH_SPACE + userObj.getUserLastName();
		// Log.info("Demote Article DAO :: User Name :::: " + userName);

		Map procRetVal = null;// new HashMap();
		procRetVal = _updateArticleStatePrc.excuteUpdateArticleStateProcedure(
				articleSeqId, ArticleConstants.ARTICLE_WF_ACTION_DEMOTE
						.toUpperCase(Locale.US), userSSO);
		if (procRetVal != null) {
			returnVal = (String) procRetVal.get(ArticleConstants.PROC_OUT_PUT);
		}
		/* procedure will return the Article Sequence ID */

		if (returnVal != null
				&& !returnVal
						.equalsIgnoreCase(ArticleConstants.PROC_OUT_PUT_ERROR)) {
			/*
			 * If procedure returned value is other than ERROR_OCCURED then send
			 * de-motion email to reviewers ...
			 */
			sendDemoteArticleMail(ectValue, artTitle, articleCode,
					articleSeqId, demotionReason, userName);
			// Log.info("Done with sending the Demote Email....");

			/*
			 * if notification list is not empty then only send the notification
			 * email...
			 */
		}
		return returnVal;
	}

	/**
	 * @param assetId
	 * @return List<SecurityModelData>
	 * @throws DRBCommonException
	 */
	public List<SecurityModelData> getArticleMetaDataForEmail(String assetId,
			String userId) throws DRBCommonException {
		// Log.info("getArticleMetaDataForEmail() in DAO.... " + assetId);

		List<SecurityModelData> retValList = null;// new
		// ArrayList<SecurityModelData>();
		RowMapper getArticleMetadataFromEmailRowMapper = new RowMapper() {
			public SecurityModelData mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				SecurityModelData secMod = new SecurityModelData();
				secMod.setAssetSeqId(rs
						.getString(ReportConstants.ARTICLE_SEQ_ID));
				secMod.setAssetCode(rs.getString(ReportConstants.ARTICLE_CODE));
				secMod.setObjectDocumentClass(rs
						.getString(ReportConstants.ARTICLE_DOC_CLASS));
				secMod.setGeneralPoolHasAccess(DRBUtils.charToBoolean(rs
						.getString(ReportConstants.ARTICLE_GENERAL_POOL_ACCESS)));
				secMod.setUserInGeneralPool(DRBUtils.charToBoolean(rs
						.getString(ReportConstants.ARTICLE_USER_IN_GEN_POOL)));
				secMod.setEctInForce(DRBUtils.charToBoolean(rs
						.getString(ReportConstants.ARTICLE_ECT_IN_FORCE)));
				secMod.setObjectEcTag(rs
						.getString(ReportConstants.ARTICLE_OBJ_EC_TAG));
				secMod.setUserHasNamedAccess(DRBUtils.charToBoolean(rs
						.getString(ReportConstants.ARTICLE_USER_HAS_NAMED_ACCESS)));
				secMod.setLegacyOrObjectCode(rs
						.getString(SearchConstants.SECMDL_LEG_OR_OBJ_CODE));
				return secMod;
			}
		};

		retValList = getJdbcTemplate().query(
				DRBQueryConstants.GET_ARTICLE_METADATA_FROM_EMAIL_QRY,
				new Object[] { userId, userId, userId, assetId },
				getArticleMetadataFromEmailRowMapper);
		// Log.info("getArticleMetaDataForEmail() in DAO.... retValList " +
		// retValList);
		return retValList;
	}

	/**
	 * @param assetId
	 * @return List<SecurityModelData>
	 * @throws DRBCommonException
	 */
	public List<SecurityModelData> getNameAccessForObject(String assetId,
			String userId, String type) throws DRBCommonException {
		// Log.info("getNameAccessForObject() in DAO.... " + assetId);
		List<SecurityModelData> retValList = null;
		if (type.equalsIgnoreCase("Article")) {
			retValList = null; // new ArrayList<SecurityModelData>();
			RowMapper getArticleMetadataFromEmailRowMapper = new RowMapper() {
				public SecurityModelData mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					SecurityModelData secMod = new SecurityModelData();
					secMod.setAssetSeqId(rs
							.getString(ReportConstants.ARTICLE_SEQ_ID));
					secMod.setObjectDocumentClass(rs
							.getString(ReportConstants.ARTICLE_DOC_CLASS));
					secMod.setGeneralPoolHasAccess(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.ARTICLE_GENERAL_POOL_ACCESS)));
					secMod.setUserInGeneralPool(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.ARTICLE_USER_IN_GEN_POOL)));
					secMod.setEctInForce(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.ARTICLE_ECT_IN_FORCE)));
					secMod.setObjectEcTag(rs
							.getString(ReportConstants.ARTICLE_OBJ_EC_TAG));
					secMod.setUserHasNamedAccess(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.ARTICLE_USER_HAS_NAMED_ACCESS)));
					secMod.setLegacyOrObjectCode(rs
							.getString(SearchConstants.SECMDL_LEG_OR_OBJ_CODE));
					return secMod;
				}
			};

			retValList = getJdbcTemplate().query(
					DRBQueryConstants.GET_ARTICLE_METADATA_FROM_EMAIL_QRY,
					new Object[] { userId, userId, userId, assetId },
					getArticleMetadataFromEmailRowMapper);
			// Log.info("getNameAccessForObject() in DAO.... retValList(Article) "
			// + retValList);
		} else {
			retValList = null;// new ArrayList<SecurityModelData>();
			RowMapper getArticleMetadataFromEmailRowMapper = new RowMapper() {
				public SecurityModelData mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					SecurityModelData secMod = new SecurityModelData();
					secMod.setAssetSeqId(rs
							.getString(ReportConstants.FOLDER_ID));
					secMod.setObjectDocumentClass(rs
							.getString(ReportConstants.FOLDER_DOC_CLASS));
					secMod.setGeneralPoolHasAccess(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.FOLDER_GENERAL_POOL_ACCESS)));
					secMod.setUserInGeneralPool(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.FOLDER_USER_IN_GENERAL_POOL)));
					secMod.setEctInForce(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.FOLDER_ECT_IN_FORCE)));
					secMod.setObjectEcTag(rs
							.getString(ReportConstants.FOLDER_OBJ_EC_TAG));
					secMod.setUserHasNamedAccess(DRBUtils.charToBoolean(rs
							.getString(ReportConstants.FOLDER_USER_HAS_NAMED_ACCESS)));
					secMod.setLegacyOrObjectCode(rs
							.getString(SearchConstants.SECMDL_LEG_OR_OBJ_CODE));
					return secMod;
				}
			};

			retValList = getJdbcTemplate().query(
					DRBQueryConstants.GET_FOLDER_METADATA_FROM_EMAIL_QRY,
					new Object[] { userId, userId, userId, assetId },
					getArticleMetadataFromEmailRowMapper);
			// Log.info("getNameAccessForObject() in DAO.... retValList(Folder) "
			// + retValList);
		}
		return retValList;
	}

	public int captureIssueArticlesHistory(ArticleData articleData) {
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String userId = userObj.getUserSSOID();
		String userName = userObj.getUserLastName() + ", "
				+ userObj.getUserFirstName();

		List<String> articleHistory = getSimpleJdbcTemplate().query(
				DRBQueryConstants.CHECK_ARTICLE_HISTORY_AVAILABLE,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("APP_DATA_HISTORY_SEQ_ID");
					}
				}, articleData.getArticleSeqid(),
				ArticleConstants.STRING_DRB_ARTICLE,
				articleData.getChangedAttributeName(),
				articleData.getOldValue(), articleData.getNewValue());

		int historyUpdate = 0;
		if (articleHistory.size() > 0) {
			String artHistSeqId = articleHistory.get(0);
			if (null != artHistSeqId && !artHistSeqId.equals("")) {
				historyUpdate = getJdbcTemplate().update(
						DRBQueryConstants.UPDATE_ARTICLE_HISTORY,
						new Object[] { userName, artHistSeqId });
			}
		} else {
			historyUpdate = getJdbcTemplate()
					.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_STATE_CHANGE_HISTORY_QUERY,
							new Object[] {
									userId,
									articleData.getChangedAttributeName(),
									articleData.getOldValue(),
									articleData.getNewValue(),
									Integer.parseInt(articleData
											.getArticleSeqid()), userName,
									userName });
		}

		return historyUpdate;
	}

	/**
	 * @param oldState
	 * @param newState
	 * @param articleSeqId
	 * @return integer
	 */
	public int captureArticlesHistory(ArticleData articleData) {
		UserData userObj = (UserData) (DRBUtils.getServletSession(false)
				.getAttribute(ArticleConstants.SESSION_USER_DATA));
		String userId = userObj.getUserSSOID();
		String userName = userObj.getUserLastName() + ", "
				+ userObj.getUserFirstName();
		int historyUpdate = getJdbcTemplate()
				.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_STATE_CHANGE_HISTORY_QUERY,
						new Object[] {
								userId,
								articleData.getChangedAttributeName(),
								articleData.getOldValue(),
								articleData.getNewValue(),
								Integer.parseInt(articleData.getArticleSeqid()),
								userName, userName });
		return historyUpdate;
	}

	/**
	 * This method is used to send the State change email to Notification list
	 * users
	 * 
	 * @param artTitle
	 * @param notifyUserMailIds
	 * @param curArtCode
	 * @param revArtCode
	 * @param articleState
	 * @param revIndica
	 * @param actFlag
	 */
	public void sendStateChangeEmail(String sexportStatus, String artSeqId,
			String artTitle, List<String> notifyUserMailIds, String curArtCode,
			String revArtCode, String articleState, String revIndica,
			String actFlag) {
		try {
			// Log.info("Article Dao IMPL  Before Sending Email to Notification list");
			String linkUrl = null;
			List<String> toMail = null;
			ArrayList<String> ccMail = new ArrayList<String>();
			StringBuffer subject = null;
			StringBuffer body = null;
			subject = new StringBuffer();
			body = new StringBuffer();

			ResourceBundle rbundle = ResourceBundle
					.getBundle(FolderConstants.ENV_PROP_FILE);
			linkUrl = rbundle.getString(FolderConstants.EMAIL_LINK);

			toMail = notifyUserMailIds;
			subject.append(ArticleConstants.ARTICLE_STATE_CHANGE_EMAIL_SUBJECT);
			subject.append(curArtCode);
			if (sexportStatus != null
					&& (sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NLR) || sexportStatus
							.equalsIgnoreCase(ArticleConstants.DRB_NSR))) {
				subject.append(ArticleConstants.DRB_DASH);
				subject.append(artTitle);
			}
			body.append(ArticleConstants.HTML_TAG);
			body.append(ArticleConstants.HTML_BODY_TAG);
			body.append(ArticleConstants.HTML_TABLE_TAG);
			body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
			body.append(ArticleConstants.ARTICLE_NO_LABLE + curArtCode
					+ ArticleConstants.ARTICLE_NUMBERED
					+ ArticleConstants.SINGLE_CODE + artTitle
					+ ArticleConstants.SINGLE_CODE);

			if (revIndica != null
					&& revIndica
							.equalsIgnoreCase(ArticleConstants.EDRB_YES_FLAG)) {
				if (actFlag != null
						&& actFlag
								.equalsIgnoreCase(ArticleConstants.ARTICLE_WF_ACTION_CANCEL)) {
					/* Action is Cancel */
					body.append(ArticleConstants.ARTICLE_STATE_CHANGED_FROM_DRAFT_TO);
					body.append(ArticleConstants.SINGLE_CODE
							+ ArticleConstants.STATE_CANCELLED
							+ ArticleConstants.SINGLE_CODE
							+ ArticleConstants.EDRB_DOT_STRING);/*
																 * Draft to
																 * Canceled
																 */
					body.append(ArticleConstants.LINE_BRAKE_TAG);
					body.append(ArticleConstants.ARTICLE_NO_LABLE + revArtCode
							+ ArticleConstants.ARTICLE_NUMBERED
							+ ArticleConstants.SINGLE_CODE + artTitle
							+ ArticleConstants.SINGLE_CODE);
					body.append(ArticleConstants.ARTICLE_STATE_CHANGED_FROM_CHECKEDOUT_TO);
					body.append(ArticleConstants.SINGLE_CODE
							+ ArticleConstants.STATE_ISSUED
							+ ArticleConstants.SINGLE_CODE
							+ ArticleConstants.EDRB_DOT_STRING);/*
																 * Checked-out
																 * to Issued
																 */
				} else if (actFlag != null
						&& actFlag
								.equalsIgnoreCase(ArticleConstants.EDRB_ACTON_FLAG01)) {
					body.append(ArticleConstants.ARTICLE_STATE_CHANGED_FROM_ISSUED_TO);
					body.append(ArticleConstants.SINGLE_CODE + articleState
							+ ArticleConstants.SINGLE_CODE
							+ ArticleConstants.EDRB_DOT_STRING);
				} else if (actFlag != null
						&& actFlag
								.equalsIgnoreCase(ArticleConstants.EDRB_ACTON_FLAG02)) {
					body.append(ArticleConstants.ARTICLE_STATE_CHANGED_FROM_DELETED_TO);
					body.append(ArticleConstants.SINGLE_CODE + articleState
							+ ArticleConstants.SINGLE_CODE
							+ ArticleConstants.EDRB_DOT_STRING);
				}
			} else {
				body.append(ArticleConstants.ARTICLE_STATE_CHANGED_FROM);
				if (actFlag != null
						&& actFlag
								.equalsIgnoreCase(ArticleConstants.ARTICLE_WF_ACTION_CANCEL)) {
					body.append(ArticleConstants.ARTICLE_DRAFT_TO);
				} else if (actFlag != null
						&& actFlag
								.equalsIgnoreCase(ArticleConstants.EDRB_ACTON_FLAG01)) {
					body.append(ArticleConstants.ARTICLE_ISSUED_TO);
				} else if (actFlag != null
						&& actFlag
								.equalsIgnoreCase(ArticleConstants.EDRB_ACTON_FLAG02)) {
					body.append(ArticleConstants.ARTICLE_DELETED_TO);
				} else if (actFlag != null
						&& actFlag
								.equalsIgnoreCase(ArticleConstants.ARTICLE_WF_ACTION_DEMOTE)) {
					body.append(ArticleConstants.ARTICLE_INREVIEW_TO);
				}
				body.append(ArticleConstants.SINGLE_CODE + articleState
						+ ArticleConstants.SINGLE_CODE
						+ ArticleConstants.EDRB_DOT_STRING);
			}
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);

			body.append(ArticleConstants.CLICK_TO_GOTO_DRB_HOME);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append("<a href=").append(linkUrl);
			body.append(" target=\"new\">");
			body.append(ArticleConstants.DRB_HOME);
			body.append("</a>");
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);

			body.append(ArticleConstants.AUTO_GENERATED
					+ ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.LINE_BRAKE_TAG);
			body.append(ArticleConstants.TABLE_ROW_END_TAG);
			body.append(ArticleConstants.HTML_TABLE_END_TAG);
			body.append(ArticleConstants.HTML_BODY_END_TAG);
			body.append(ArticleConstants.HTML_END_TAG);
			// added by santosh on 5 dec start
			// UserData userObj = (UserData)
			// (DRBUtils.getServletSession(false).getAttribute("USER_DATA"));
			// LOG.info("*****user id******"+userObj.getUserEMailID());
			// toMail.add(userObj.getUserEMailID()); commented to avoid
			// dubplicate mail
			// LOG.info("*****mail size id******"+toMail.size());
			// added by santosh on 5 dec end
			_mailBean.sendMail(toMail, ccMail, subject.toString(),
					body.toString(), _mailSend, ArticleConstants.TEST);
			// Log.info("Article Dao IMPL ::: After sending email to notification list");
			body = null;// heap issue
		} catch (MailSendException e) {// Code Review Change By Naresh on
										// 22DEC10
			LOG.error("Exception[MailSendException]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		} catch (Exception e) {
			LOG.error("Exception[sendStateChangeEmail]" + e.getMessage());
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, e.getMessage());
		}
	}

	/**
	 * @param namList
	 * @return List<UserData>
	 * @throws DRBCommonException
	 */
	public List<UserData> getNameForId(List<String> namList)
			throws DRBCommonException {
		return _userServiceObj.getUserDetails(namList);
	}

	/**
	 * @param recpId
	 * @param revComments
	 * @param updatedBy
	 * @throws DRBCommonException
	 */
	public void updateTaggerTask(String recpId, String revComments,
			String updatedBy, String reqSeqId) throws DRBCommonException {
		getSimpleJdbcTemplate().update(DRBQueryConstants.UPDATE_TAGGER_REQ,
				recpId, revComments, updatedBy, reqSeqId);
	}

	/**
	 * This Method will check the Cancel and Delete states of the selected
	 * article before pasting an article into target location
	 * 
	 * @param artSeqId
	 * @return String
	 * @throws DRBCommonException
	 */
	public String checkArticleCancelorDeleted(String artSeqId)
			throws DRBCommonException {
		// Log.info("Inside checkArticleCancelorDeleted() $$$$$$$$$$$ ");
		String retVal = ArticleConstants.CHECKLIST_SAVE_SUCCESS;
		RowMapper getArticleLocationRowMapper = new RowMapper() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String retVal = rs.getString("CANCEL_DATE")
						+ ArticleConstants.TILDA + rs.getString("DELETE_DATE");
				return retVal;
			}
		};
		List list = getJdbcTemplate().query(
				DRBQueryConstants.CHECK_ARTICLE_CANCEL_OR_DELETED,
				new Object[] { artSeqId }, getArticleLocationRowMapper);

		String[] listToArray = null;
		String cancelIndica = null;
		String deleteIndica = null;
		if (list != null) {
			listToArray = list.get(0).toString().split(ArticleConstants.TILDA);
			cancelIndica = listToArray[0];
			deleteIndica = listToArray[1];
		}

		if (cancelIndica != null && !"".equalsIgnoreCase(cancelIndica)
				&& !"null".equalsIgnoreCase(cancelIndica)) {
			retVal = ArticleConstants.EDRB_ACTON_FLAG04;
		}
		if (deleteIndica != null && !"".equalsIgnoreCase(deleteIndica)
				&& !"null".equalsIgnoreCase(deleteIndica)) {
			retVal = ArticleConstants.EDRB_ACTON_FLAG06;
		}
		// Log.info("checkArticleCancelorDeleted() $$$$$$$$$$$$$$$$$ Value Getting returned ==>>> "
		// + retVal);
		return retVal;
	}

	/**
	 * @param artSeqId
	 *            End:Added by Pradeep for Enhancement No: 68
	 * @throws DRBCommonException
	 */

	/**
	 * @param artSeqId
	 * @return
	 * @throws DRBCommonException
	 */
	public List<ArticleAmendData> previousVersions(String artSeqId)
			throws DRBCommonException {
		RowMapper preVerRowMapper = new RowMapper() {
			public ArticleAmendData mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				// Log.info("P_OUT_PREV_ARTCLS_LIST ----->>>>");
				ArticleAmendData _at = new ArticleAmendData();
				_at.setArticleTitle(rs.getString("ARTICLE_TITLE"));
				if (rs.getString("ARTICLE_SEQ_ID") != null) {
					_at.setArticleSeqId(rs.getString("ARTICLE_SEQ_ID"));
				} else {
					_at.setArticleSeqId("");
				}
				_at.setFamilyName(rs.getString("OWNER_USER_FIRST_NAME"));
				_at.setGivenName(rs.getString("OWNER_USER_LAST_NAME"));
				_at.setArticleOwner(_at.getFamilyName() + ", "
						+ _at.getGivenName());
				_at.setCreationDate(rs.getDate("CREATION_DATE"));
				_at.setUserID(rs.getString("OWNER_USER_ID"));
				_at.setPreviousArticleCode(rs.getString("ARTICLE_CODE"));
				return _at;
			}
		};
		List<ArticleAmendData> preVersion = getJdbcTemplate().query(
				DRBQueryConstants.PRE_VERSIONS_ARTS,
				new Object[] { artSeqId, artSeqId }, preVerRowMapper);
		return preVersion;
	}

	/**
	 * @param ownerSSOid
	 * @param libraryId
	 * @param additionalReviewerId
	 * @param articleSeqId
	 * @param reviewerid
	 * @return
	 * @throws DRBCommonException
	 */
	@Transactional
	public void removeArticleAdditionalReviewer(String ownerSSOid,
			String libraryId, String additionalReviewerId, String articleSeqId,
			String reviewerid) throws DRBCommonException {
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.REMOVE_ADDITIONAL_REVIEWER_TASK,
				articleSeqId, additionalReviewerId);
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.REMOVE_ADDITIONAL_REVIEWER, articleSeqId);
	}

	/**
	 * @param seqId
	 * @param newTaggerSso
	 * @param modifiedBy
	 * @return int
	 */
	public int updateNamedTagger(String seqId, String newTaggerSso,
			String modifiedBy, String assetType) throws DRBCommonException {
		int flag = NEGATIVEINDEX;
		if ((ECTagConstants.ARTICLE).equalsIgnoreCase(assetType)) {
			flag = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_NAMED_TAGGER_ARTICLE,
					newTaggerSso, modifiedBy, seqId);
		} else if ((ECTagConstants.FOLDER).equalsIgnoreCase(assetType)) {
			flag = getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_NAMED_TAGGER_FOLDER, newTaggerSso,
					modifiedBy, seqId);
		}
		// Log.info("Update Flag :::" + flag);
		return flag;
	}

	/**
	 * Method to get DRB taggers.
	 * 
	 */
	public List<UserData> getTaggersWithRoles(String ownerSsoid,
			String libSeqId, String searchName, String frontName,
			String rearName) throws DRBCommonException {
		List<UserData> taggers = _namedTaggerProcedure.excuteNamedTaggerProc(
				ownerSsoid, libSeqId, searchName, frontName, rearName);
		return taggers;
	}

	/**
	 * @return the namedTaggerProcedure
	 */
	public NamedTaggerProcedure getNamedTaggerProcedure() {
		return _namedTaggerProcedure;
	}

	/**
	 * @param namedTaggerProcedure
	 *            the namedTaggerProcedure to set
	 */
	public void setNamedTaggerProcedure(
			NamedTaggerProcedure namedTaggerProcedure) {
		this._namedTaggerProcedure = namedTaggerProcedure;
	}

	/**
	 * Added by pradeep for Enhancement No: 100 and Ref: 5.5.11
	 */
	// added on 7 jan start
	public String getFolderCode(String folderSeqId) throws DRBCommonException {
		// //LOG.INFO("****** ArticldDaoImpl*******"+folderSeqId);
		String folderCode = (String) getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.FOLDER_ID,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						String foldercodedb = rs.getString("FOLDER_CODE");
						return foldercodedb;
					}
				}, folderSeqId);
		// //LOG.INFO("****** folderCode *******"+folderCode);
		return folderCode;

	}

	// added on 7 jan end
	/**
	 * Added by pradeep for Enhancement No: 57
	 */

	public String getLibSeqId(String libSeqId) throws DRBCommonException {
		// //LOG.INFO("****** InsideArticldDaoImpl*******"+libSeqId);
		// //LOG.INFO("@@@@@@@@@@@@JDBCTEMPLATE@@@@@@@@@@@"+getSimpleJdbcTemplate());
		String libCode = (String) getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.LIB_SEQ_ID,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						String libcodedb = rs.getString("LIBRARY_SEQ_ID");
						return libcodedb;
					}
				}, libSeqId);
		// //LOG.INFO("****** lib *******"+libCode);
		return libCode;
	}

	/**
	 * Added by pradeep for Enhancement No: 68
	 */

	public String setBookMarkDetails(String articleSeqId, String ssoId,
			String createdBy, String lastUpdatedBy) throws DRBCommonException {
		int insertBookMarkDetails = 0;
		String resVal = null;

		insertBookMarkDetails = getSimpleJdbcTemplate().update(
				DRBQueryConstants.INSERT_BOOK_MARK_DETAILS, articleSeqId,
				ssoId, createdBy, lastUpdatedBy);

		if (insertBookMarkDetails != 0) {
			resVal = "Y";
		} else {
			resVal = "N";
		}
		return resVal;
	}

	// Modified By Santosh on 12JAN11 for CancelArticle from Myfavourite
	// Modified By Santosh on 26-01-2011 for Delete Article from Myfavourite
	public List<FolderMetaData> getAllMyFavorites(String userId)
			throws DRBCommonException {
		List<FolderMetaData> accessLst = null;
		List<FolderMetaData> finalAccessLst = null;
		finalAccessLst = new ArrayList<FolderMetaData>();
		accessLst = getSimpleJdbcTemplate().query(
				DRBQueryConstants.GET_ALL_MY_FAVORITES,
				bookmarkslist,
				new Object[] { userId, userId, userId, userId, userId, userId,
						userId, userId });
		for (FolderMetaData listAfterRemoveCancel : accessLst) {
			if (FolderConstants.ART_CANCEL
					.equalsIgnoreCase(listAfterRemoveCancel
							.getArticleStateName())
					|| FolderConstants.ART_DEL
							.equalsIgnoreCase(listAfterRemoveCancel
									.getArticleStateName())) {

				getSimpleJdbcTemplate().update(
						DRBQueryConstants.GET_REMOVE_FAVORITES, userId,
						listAfterRemoveCancel.getArticleFolderSeqId());
			} else {
				finalAccessLst.add(listAfterRemoveCancel);
				listAfterRemoveCancel = null;
			}
		}
		accessLst = null;
		return finalAccessLst;
	}

	/**
	 * This method deletes the fav queries checked by the logged in user
	 * 
	 * @param list
	 * @return boolean
	 */
	public boolean deleteSavedFavQueries(String artseqId, String usersso)
			throws DRBCommonException {
		boolean isDeleted = false;
		int count = 0;
		StringBuffer queryBuffer = new StringBuffer()
				.append(DRBQueryConstants.DELETE_FAV_FOLDERARTICLES);
		queryBuffer.append("('");
		queryBuffer.append(artseqId);
		queryBuffer.append("')");
		queryBuffer.append(" AND TRIM(USER_ID) = ?");
		String query = queryBuffer.toString();
		count = getSimpleJdbcTemplate().update(query, new Object[] { usersso });
		if (count != 0) {
			isDeleted = true;
		}
		return isDeleted;
	}

	public boolean validateBookMarkDetails(String artseqId, String usersso)
			throws DRBCommonException {
		boolean isFound = false;
		int noOfRows;

		try {
			noOfRows = getSimpleJdbcTemplate().queryForInt(
					DRBQueryConstants.CHECK_ARTICLE_EXISTS, artseqId, usersso);
			if (noOfRows > 0) {
				isFound = true;
			}

		} catch (Exception e) {

			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);
		}

		return isFound;
	}

	/**
	 * End: Added by Pradeep on 06-09-2010
	 */

	// Modified by Pradeep on 02/11/2010
	// added by santosh for document classifications start
	@Transactional
	public String updateArticleDoc(String ectValue,
			Map<String, String> mapParam, UserData userObj)
			throws DRBCommonException {
		String userName = userObj.getUserLastName()
				+ ArticleConstants.SEPARATOR + userObj.getUserFirstName();
		String retVal = null;
		String emailsIds = null;
		int val = 0;
		Map messageMap = null;

		if ((String) mapParam.get("userRole") != null) {
			String articleCode = "notused";
			try {
				if (((String) mapParam.get("propEnable")).equals("Y")) {
					messageMap = _changeArticleClassificationAllPrc
							.executeChangeArticleClassificationAll(Integer
									.parseInt((String) mapParam
											.get("articleSeqId")),
									(String) userObj.getUserSSOID(),
									(String) mapParam
											.get("geDocumentClass_Number"),
									userName);

				} else {
					messageMap = _changeArticleClassificationPrc
							.executeChangeArticleClassification(
									Integer.parseInt((String) mapParam
											.get("articleSeqId")),
									(String) userObj.getUserSSOID(),
									articleCode,
									(String) mapParam
											.get("geDocumentClass_Number"),
									userName,
									ArticleConstants.CHANGE_DOC_REQUEST_DISCRIPTION);
				}
				if (messageMap != null)
					emailsIds = (String) messageMap.get("P_OUT_PROC_EXEC_MSG");
				if (emailsIds != null
						&& emailsIds.contains(ArticleConstants.TILDA)) {
					val = 1;
				} else if (emailsIds != null
						&& emailsIds
								.contains(ArticleConstants.PROPAGATE_ERROR_MESSAGE_DB)) {
					val = 2;
				} else {
					val = 0;
				}

				/**
				 * End: Added by Pradeep on 02/10/2010 for History Log of Change
				 * document Classification
				 */
			} catch (DRBCommonException exp) {// Code Review Changed By Naresh
												// on 22DEC10
				LOG.error("Exception[updateArticleDoc]" + exp.getMessage());
				DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);
			} catch (Exception e) {
				// LOG.error("Exception[updateArticleDoc]" + e.getMessage());
				DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);
			}

			// Added by Avinash
			if (val == 2) {
				retVal = ArticleConstants.PROPAGATE_ERROR_MESSAGE;
			}// Avinash Code End.
			else if (val != 0 && ((String) mapParam.get("userRole") != null)) {
				retVal = ArticleConstants.SAVE_SUCCESS;
			}
		} else {
			retVal = ArticleConstants.ERROR;
		}
		// added by santosh for doc
		return retVal;
	}

	// For ENH # 52 end
	/**
	 * Start: Added by Pradeep for Document Classification on 02-11-2010
	 */
	/**
	 * @return the _changeArticleClassificationPrc
	 */
	public ChangeArticleClassificationProcedure getChangeArticleClassificationPrc() {
		return _changeArticleClassificationPrc;
	}

	/**
	 * @param _changeArticleClassificationPrc
	 *            the changeArticleClassificationPrc to set
	 */
	public void setChangeArticleClassificationPrc(
			ChangeArticleClassificationProcedure changeArticleClassificationPrc) {
		this._changeArticleClassificationPrc = changeArticleClassificationPrc;
	}

	/**
	 * End: Added by Pradeep for Document Classification on 02-11-2010
	 */
	// added on 11 jan for removing cancel article records
	public void removeCancelArticleContents(String articleSeqId)
			throws DRBCommonException {
		// LOG.info("*****before***by santosh======");
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.REMOVE_CANCEL_ARTICLE_RECORDS, articleSeqId);
		// LOG.info("*****after***by santosh======");
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.REMOVE_CANCEL_ARTICLE_HISTORY, articleSeqId);
		// LOG.info("*****REMOVE_CANCEL_ARTICLE_HISTORY*******");
	}

	/**
	 * Start: Added by Pradeep for Defect Id: 1455 on 28-01-2011
	 */

	public String getTitleOld() {
		return _titleOld;
	}

	public void setTitleOld(String old) {
		_titleOld = old;
	}

	/**
	 * End: Added by Pradeep for Defect Id: 1455 on 28-01-2011
	 */

	/**
	 * Added by Pradeep for Defect Id: 1464 on 03-02-2011
	 */

	public String getDesignMetaDataIndReq(String libSeqId) {
		/*
		 * LOG.info("****** InsideArticldDaoImpl*******"+libSeqId);
		 * LOG.info("@@@@@@@@@@@@JDBCTEMPLATE@@@@@@@@@@@"
		 * +getSimpleJdbcTemplate());
		 */
		String libCode = (String) getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.DES_META_DATA_REQ_IND,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						String desmeta = rs
								.getString("DESIGN_METADATA_REQ_IND");
						return desmeta;
					}
				}, libSeqId);
		/* LOG.info("****** lib *******"+libCode); */
		return libCode;
	}

	public String getArticleWfStateId(String articleSeqId) {
		// LOG.info("****** InsideArticldDaoImpl*******"+articleSeqId);
		// LOG.info("@@@@@@@@@@@@JDBCTEMPLATE@@@@@@@@@@@"+getSimpleJdbcTemplate());
		String state_name = (String) getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.ARTICLE_STATE_IND,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						String desmeta = rs.getString("STATE_NAME");
						return desmeta;
					}
				}, articleSeqId);
		// LOG.info("****** state_name *******"+state_name);
		return state_name;
	}

	public String getUserGeneralPool(String seqId, String userId,
			String assetType) {
		String userInGeneralPool = null;

		String GET_GENERAL_POOL_FOR_USER_IN_ARTICLE = "SELECT DECODE((SELECT COUNT(1) FROM CAWC.DRB_USER_LIBRARY_GROUP_V A "
				+ "INNER JOIN CAWC.DRB_LIBRARY_GROUP B ON A.LIBRARY_GROUP_SEQ_ID = B.LIBRARY_GROUP_SEQ_ID INNER JOIN DRB_ARTICLE AF "
				+ "ON AF.LIBRARY_SEQ_ID = B.LIBRARY_SEQ_ID  WHERE AF.ARTICLE_SEQ_ID = '"
				+ seqId
				+ "' "
				+ "AND B.GROUP_NAME = 'GENERAL POOL' AND A.USER_ID =  '"
				+ userId + "'), 0,'N','Y') " + "User_In_Gen_Pool FROM DUAL ";
		if ("Article".equals(assetType)) {
			userInGeneralPool = (String) getSimpleJdbcTemplate()
					.queryForObject(GET_GENERAL_POOL_FOR_USER_IN_ARTICLE,
							new ParameterizedRowMapper<String>() {
								public String mapRow(ResultSet rs, int rowNum)
										throws SQLException {
									String desmeta = rs
											.getString("USER_IN_GEN_POOL");
									return new String(desmeta);
								}
							}, new Object[] {});
		}
		return userInGeneralPool;
		// LOG.info("****** state_name *******"+state_name);

	}

	public FolderMetaData getOwnerForObjectCode(String objectCode,
			String assetType) {
		FolderMetaData theFolderMetaData = null;
		if ("Article".equalsIgnoreCase(assetType)) {
			theFolderMetaData = (FolderMetaData) getSimpleJdbcTemplate()
					.queryForObject(
							DRBQueryConstants.GET_OWNER_FOR_ARTICLE_CODE,
							new ParameterizedRowMapper<FolderMetaData>() {
								public FolderMetaData mapRow(ResultSet rs,
										int rowNum) throws SQLException {
									String ownerUserId = rs
											.getString("OWNER_USER_ID");
									String libSeqid = rs
											.getString("LIBRARY_SEQ_ID");
									String stateName = rs
											.getString("STATE_NAME");
									FolderMetaData folderData = new FolderMetaData();
									folderData.setOwnerUserId(ownerUserId);
									folderData.setLibrarySeqId(libSeqid);
									folderData.setResultState(stateName);
									return folderData;
								}
							}, objectCode);
		} else if ("Folder".equalsIgnoreCase(assetType)) {
			theFolderMetaData = (FolderMetaData) getSimpleJdbcTemplate()
					.queryForObject(
							DRBQueryConstants.GET_OWNER_FOR_FOLDER_CODE,
							new ParameterizedRowMapper<FolderMetaData>() {
								public FolderMetaData mapRow(ResultSet rs,
										int rowNum) throws SQLException {
									String ownerUserId = rs
											.getString("OWNER_USER_ID");
									String libSeqid = rs
											.getString("LIBRARY_SEQ_ID");
									FolderMetaData folderData = new FolderMetaData();
									folderData.setOwnerUserId(ownerUserId);
									folderData.setLibrarySeqId(libSeqid);
									return folderData;
								}
							}, objectCode);
		}

		// LOG.info("*****article adoimpl success*****");
		return theFolderMetaData;
	}

	// Added by Avinash
	public ChangeArticleClassificationAllProcedure getChangeArticleClassificationAllPrc() {
		return _changeArticleClassificationAllPrc;
	}

	public void setChangeArticleClassificationAllPrc(
			ChangeArticleClassificationAllProcedure changearticleClassificationAllPrc) {
		this._changeArticleClassificationAllPrc = changearticleClassificationAllPrc;
	}

	/**
	 * @param requestorId
	 * @param articleSeqId
	 * @return String
	 */
	public String checkIfAlreadyRequestedECAccess(String requestorId,
			String articleSeqId, String assetType) throws DRBCommonException {
		String errMsg = null;
		try {
			List<Integer> reqSeqId;
			ParameterizedRowMapper<Integer> getCount = new ParameterizedRowMapper<Integer>() {
				public Integer mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					int reqVal = rs.getInt(1);
					return reqVal;
				}
			};
			String query = null;
			if (assetType != null
					&& assetType.equalsIgnoreCase(ArticleConstants.ARTICLE)) {
				query = DRBQueryConstants.CHECK_EXISTING_EC_REQUEST_ARTICLE;
			} else if (assetType != null
					&& assetType.equalsIgnoreCase(ArticleConstants.FOLDER)) {
				query = DRBQueryConstants.CHECK_EXISTING_EC_REQUEST_FOLDER;
			}
			reqSeqId = getSimpleJdbcTemplate().query(query, getCount,
					articleSeqId, requestorId);

			if (reqSeqId.get(0) != 0) {
				errMsg = "You have already applied for Tagger role for this ";
				if (assetType != null
						&& assetType.equalsIgnoreCase(ArticleConstants.ARTICLE)) {
					errMsg = errMsg.concat(" Article ");
				} else if (assetType != null
						&& assetType.equalsIgnoreCase(ArticleConstants.FOLDER)) {
					errMsg = errMsg.concat(" Folder ");
				}
				errMsg = errMsg.concat(".Request is in process.");

			} else {
				errMsg = "noEcAccess";
			}
		} catch (DataRetrievalFailureException e) {
			LOG.error("An Error has occured in ArticleDaoImp :: checkIfAlreadyRequestedECAccess >>> "
					+ e.getMessage());
		}
		return errMsg;
	}

	// Added by vikas
	public String importDicData(List<DictionaryData> dictData, int libSeqId,
			String user) throws DRBCommonException {
		Object[] obj = null;
		String msg = null;
		List<DictionaryData> dictionData = dictData;
		DictionaryData dict = null;
		Iterator it = dictionData.iterator();
		CallableStatement cs = null;
		String procName = null;
		Connection con = null;
		try {
			procName = "{call CAWC.DRB_IMPORT_DICTONARY_PRC(?,?,?,?,?)}";
			ARRAY[] customWordArray = null;
			con = getJdbcTemplate().getDataSource().getConnection();
			cs = con.prepareCall(procName);
			List<String> libSeqList = new ArrayList<String>();
			while (it.hasNext()) {
				dict = (DictionaryData) it.next();
				String objcutomWord = dict.getStrCustomeWord();
				libSeqList.add(objcutomWord);

			}
			Object[] customWord = (Object[]) libSeqList.toArray();

			ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor(
					"DRB_TYP_TAB_DICT_WORD", con.getMetaData().getConnection());

			ARRAY sqlArray = new oracle.sql.ARRAY(arrayDescriptor, con
					.getMetaData().getConnection(), customWord);

			// customWordArray[0] = new ARRAY(arrayDescriptor,
			// con.getMetaData().getConnection(), customWord);
			cs.setArray(1, sqlArray);
			// cs.setString(1,"Vikas");
			cs.setInt(2, libSeqId);// need to pass lib seq id
			cs.setString(3, user);
			cs.registerOutParameter(4, OracleTypes.NUMBER);
			cs.registerOutParameter(5, OracleTypes.VARCHAR);
			cs.execute();
			if (cs.getInt(4) == 1) {
				msg = "success";
			} else {
				msg = cs.getString(5);
			}
		} catch (SQLException e) {

			e.getMessage();
		} catch (Exception e) {

			e.getMessage();
		}

		finally {
			try {
				if (con != null) {
					con.close();
				}

			} catch (SQLException exception) {
				LOG.error(exception.getMessage());
				throw new DRBCommonException(exception);
			}
			try {

				if (cs != null) {
					cs.close();
				}
			} catch (SQLException exception) {
				LOG.error(exception.getMessage());
				throw new DRBCommonException(exception);
			}

		}

		return msg;
	}

	// Added by vikas for deleting old named tagger
	public void deleteTaskfromOldTaggar(String oldSsoId, String articleSeqId) {
		Object[] args = { articleSeqId, oldSsoId };
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.DELETE_OLD_TAGGER_ARTICLE_FOLDER, args);

	}

	public void deleteTaskfromOldTaggarFolder(String oldSsoId,
			String articleSeqId) {
		Object[] args = { articleSeqId, oldSsoId };
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.DELETE_OLD_NAMED_TAGGER_ARTICLE_FOLDER, args);

	}

	public void deleteNamedTagger(String articleSeqId) {
		Object[] args = { articleSeqId };
		getSimpleJdbcTemplate().update(DRBQueryConstants.DELETE_NAMED_TAGGER,
				args);

	}

	/* Start : Added for bulk Artice owner change */

	/**
	 * @param ssoIdArticleOwner
	 * @param libraryId
	 * @param folderId
	 * @return List<Map<String, String>>
	 * @throws DRBCommonException
	 */
	@Transactional
	public List<ArticleData> getArticleDataForOwner(String ssoIdArticleOwner,
			String libraryId, String folderId) throws DRBCommonException {

		List<ArticleData> articleDataForOwner = getSimpleJdbcTemplate().query(
				DRBQueryConstants.GET_ARTICLEDATA_FOR_OWNER,
				new ParameterizedRowMapper<ArticleData>() {
					public ArticleData mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ArticleData articleData = new ArticleData();
						articleData.setArticleSeqId(rs
								.getInt(ArticleConstants.ARTICLE_SEQ_ID_COL));
						articleData.setArticleCode(rs
								.getString(ArticleConstants.ARTICLE_CODE_COL));
						articleData.setReviewerid(rs
								.getString(ArticleConstants.ARTICLE_REVIEWER_COL));
						articleData.setAdditionalReviewerId(rs
								.getString(ArticleConstants.ARTICLE_ADDITIONAL_REVIEWER_COL));
						articleData.setRevisionArticleSeqId(rs
								.getString(ArticleConstants.REVISION_ARTICLE_SEQ_ID_COL));
						articleData.setVersionNumber(rs
								.getInt(ArticleConstants.VERSION_NUMBER_COL));
						articleData.setArticleState(rs
								.getString(ArticleConstants.STATE_NAME_COL));
						articleData.setGeDocumentClass(rs
								.getString(ArticleConstants.DATA_CLASS_NAME_COL));
						return articleData;
					}
				}, ssoIdArticleOwner, libraryId, folderId);
		return articleDataForOwner;
	}

	/**
	 * @param articleIds
	 * @return int
	 * @throws DRBCommonException
	 */
	@Transactional
	public int getRestrictedArticleCount(String articleIds)
			throws DRBCommonException {
		StringBuffer selectQuery = null;
		int val = 0;
		selectQuery = new StringBuffer(
				DRBQueryConstants.GET_RESTRICTED_ARTICLE_COUNT);
		selectQuery.append(DRBQueryConstants.OPENING_BRACKET);
		selectQuery.append(articleIds);
		selectQuery.append(DRBQueryConstants.CLOSING_BRACKET);
		val = getSimpleJdbcTemplate().queryForInt(selectQuery.toString());
		return val;
	}

	/**
	 * @param newSsoId
	 * @param oldSsoId
	 * @param libraryId
	 * @param folderId
	 * @param ownerId
	 * @param oldArticleOwner
	 * @param newArticleOwner
	 * @param ownerName
	 * @param articleIds
	 * @param articleCodes
	 * @param ownerEmail
	 * @param oldOwnerEmail
	 * @return String
	 * @throws DRBCommonException
	 */
	@Transactional
	public String updateArticleBulkOwnerPack(String newSsoId, String oldSsoId,
			String libraryId, String folderId, String ownerId,
			String oldArticleOwner, String newArticleOwner, String ownerName,
			String articleIds, List<String> articleCodes,
			String articleChildIds, String ownerEmail, String oldOwnerEmail)
			throws DRBCommonException {

		StringBuffer updateArticleQuery = null;
		String retVal = null;
		int updatedArticlesCount = 0;
		updateArticleQuery = new StringBuffer(
				DRBQueryConstants.UPDATE_BULK_ARTICLE_OWNER);
		updateArticleQuery.append(DRBQueryConstants.OPENING_BRACKET);
		updateArticleQuery.append(articleIds);
		updateArticleQuery.append(DRBQueryConstants.CLOSING_BRACKET);

		updatedArticlesCount = getSimpleJdbcTemplate().update(
				updateArticleQuery.toString(), new Object[] { newSsoId });
		if (updatedArticlesCount > 0) {

			StringBuffer insertQuery = null;
			int insertAppDataCount = 0;
			insertQuery = new StringBuffer(
					DRBQueryConstants.INSERT_APP_DATA_HISTORY);
			insertQuery.append(DRBQueryConstants.OPENING_BRACKET);
			insertQuery.append(articleIds);
			insertQuery.append(DRBQueryConstants.CLOSING_BRACKET);

			insertAppDataCount = getSimpleJdbcTemplate().update(
					insertQuery.toString(),
					new Object[] { ownerId, oldArticleOwner, newArticleOwner,
							ownerName, ownerName });

			StringBuffer updateRequestQuery = null;
			int updateRequestCount = 0;
			updateRequestQuery = new StringBuffer(
					DRBQueryConstants.UPDATE_REQUEST_EVENT_WITH_ARTICLEID);
			updateRequestQuery.append(DRBQueryConstants.OPENING_BRACKET);
			updateRequestQuery.append(articleIds);
			updateRequestQuery.append(DRBQueryConstants.CLOSING_BRACKET);
			updateRequestCount = getSimpleJdbcTemplate().update(
					updateRequestQuery.toString(),
					new Object[] { newSsoId, oldSsoId });
			sendArticleOwnerBulkChangeMail(ownerEmail, oldOwnerEmail,
					articleCodes, articleChildIds, ownerName);
			retVal = updatedArticlesCount + "~" + insertAppDataCount + "~"
					+ updateRequestCount;

		} else {
			retVal = ArticleConstants.NO_ARTICLE_TO_UPDATE;
		}
		return retVal;
	}

	public void sendArticleOwnerBulkChangeMail(String ownerEmail,
			String oldOwnerEmail, List<String> articleNumbers,
			String articleSeqIds, String ownerName) throws DRBCommonException {
		List<String> toMail = new ArrayList<String>();
		List<String> ccMail = new ArrayList<String>();
		StringBuffer subject = new StringBuffer();
		StringBuffer body = new StringBuffer();
		boolean multiple = false;
		if (articleNumbers.size() > 1) {
			multiple = true;
		}
		articleSeqIds = articleSeqIds.replaceAll("'", "");
		String[] articleSeqId = DRBUtils.split(articleSeqIds, ",");

		String linkUrl = DRBUtils.getEmailUrl("EMAIL_LINK");
		toMail.add(ownerEmail);
		ccMail.add(oldOwnerEmail);
		if (multiple) {
			subject.append(ArticleConstants.CHANGEARTICLES_OWNER_EMAIL_SUBJECT_OWNER);
		} else {
			subject.append(ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_SUBJECT_OWNER);
		}
		body.append(ArticleConstants.HTML_TAG);
		body.append(ArticleConstants.HTML_BODY_TAG);
		body.append(ArticleConstants.HTML_TABLE_TAG);
		body.append(ArticleConstants.HTML_TABLE_ROW_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_BODY1);
		if (multiple) {
			body.append(ArticleConstants.ARTICLES_NO_LABLE);
		} else {
			body.append(ArticleConstants.ARTICLE_NO_LABLE);
		}
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		int count = 0;
		for (String articleNumber : articleNumbers) {
			body.append(ArticleConstants.AHREF).append(linkUrl)
					.append("?articleSeqID=").append(articleSeqId[count++]);
			body.append(" target=\"new\">");
			body.append(articleNumber);
			body.append("</a>");
			body.append(ArticleConstants.LINE_BRAKE_TAG);
		}

		if (multiple) {
			body.append(ArticleConstants.CHANGEARTICLES_OWNER_EMAIL_BODY2_BY_OWNER);
		} else {
			body.append(ArticleConstants.CHANGEARTICLE_OWNER_EMAIL_BODY2_BY_OWNER);
		}
		body.append(ownerName);
		body.append(ArticleConstants.EDRB_DOT_STRING);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.AUTO_GENERATED);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.TABLE_ROW_END_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.LINE_BRAKE_TAG);
		body.append(ArticleConstants.HTML_TABLE_END_TAG);
		body.append(ArticleConstants.HTML_BODY_END_TAG);
		body.append(ArticleConstants.HTML_END_TAG);
		_mailBean.sendMail(toMail, ccMail, subject.toString(), body.toString(),
				_mailSend, ArticleConstants.TEST);
		body = null;// heap issue
	}

	/* End : Added for Bulk Artice Owner Change */
	public void updateAritcleMyTask(String artSeqId, String reviewerId,
			String additionalReviewerId, String userId) {

		try {
			if (null != userId && userId.equals(reviewerId)) {
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_REVIEWER_DISPOSITION,
						ArticleConstants.APPROVED,
						ArticleConstants.APPROVED_WITH_VERIFICATION_PENDING,
						artSeqId);
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.UPDATE_REQUESTEVENT_APPROVE_FOR_REVIEW_ARTICLE,
								ArticleConstants.CHAR_N, reviewerId, null,
								ArticleConstants.ARTICLE_REQUEST_TYPE_NAME,
								artSeqId);
			} else if (null != userId && userId.equals(additionalReviewerId)) {
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.UPDATE_ADDREVIEWER_DISPOSITION,
						ArticleConstants.APPROVED,
						ArticleConstants.APPROVED_WITH_VERIFICATION_PENDING,
						artSeqId);
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.UPDATE_REQUESTEVENT_APPROVE_FOR_REVIEW_ARTICLE,
								ArticleConstants.CHAR_N, null,
								additionalReviewerId,
								ArticleConstants.ARTICLE_REQUEST_TYPE_NAME,
								artSeqId);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.info("Exeption caught while  updateAritcleMyTask for artSeqId: "
					+ artSeqId);
			LOG.info("Error is: " + ex.getMessage());
		}

	}

	/**
	 * @param articleSeqId
	 * @param articleData
	 * @param userSSOID
	 * @return
	 * @throws DRBCommonException
	 */
	@Transactional
	public void updateAritcleMyTaskReviewState(String articleSeqId,
			ArticleData articleData, String userSSOID)
			throws DRBCommonException {

		if (((articleData.getAdditionalReviewerId() != null) && (!(articleData
				.getAdditionalReviewerId().equals(""))))
				&& ((articleData.getAdditionalReviewer() != null) && (!(articleData
						.getAdditionalReviewer().equals(""))))) {
			String ReviewerId = null;

			if (null != userSSOID
					&& userSSOID.equals(articleData.getReviewerid())) {
				ReviewerId = articleData.getReviewerid();
				if (articleData.getAdditionalReviewerDisposition() != null
						&& (articleData.getAdditionalReviewerDisposition()
								.equals(ArticleConstants.APPROVED))) {
					getSimpleJdbcTemplate()
							.update(DRBQueryConstants.UPDATE_REVIEWER_DISPOSITION_REVIEW_STATE,
									ArticleConstants.APPROVED,
									ArticleConstants.APPROVED_WITH_VERIFICATION_PENDING,
									articleData.getReviewerid(), "",
									articleSeqId);
				} else {
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.PRIMARY_REVIEWER_APPROVAL,
							ArticleConstants.APPROVED,
							articleData.getReviewerid(), "",
							articleData.getArticleSeqid());

				}
			} else if (null != userSSOID
					&& userSSOID.equals(articleData.getAdditionalReviewerId())) {

				ReviewerId = articleData.getAdditionalReviewerId();
				if (articleData.getReviewerDisposition() != null
						&& (articleData.getReviewerDisposition()
								.equals(ArticleConstants.APPROVED))) {
					getSimpleJdbcTemplate()
							.update(DRBQueryConstants.UPDATE_ADDREVIEWER_DISPOSITION_REVIEW_STATE,
									ArticleConstants.APPROVED,
									ArticleConstants.APPROVED_WITH_VERIFICATION_PENDING,
									articleData.getAdditionalReviewerId(), "",
									articleSeqId);
				} else {
					getSimpleJdbcTemplate().update(
							DRBQueryConstants.ADDITIONAL_REVIEWER_APPROVAL,
							ArticleConstants.APPROVED,
							articleData.getAdditionalReviewerId(), "",
							articleData.getArticleSeqid());
				}
			}

			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.UPDATE_REQUESTEVENT_APPROVE_FOR_REVIEW_ARTICLE,
							ArticleConstants.CHAR_N, ReviewerId, null,
							ArticleConstants.ARTICLE_REQUEST_TYPE_NAME,
							articleSeqId);

		} else {
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_REVIEWER_DISPOSITION_REVIEW_STATE,
					ArticleConstants.APPROVED,
					ArticleConstants.APPROVED_WITH_VERIFICATION_PENDING,
					articleData.getReviewerid(), "", articleSeqId);
			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.UPDATE_REQUESTEVENT_APPROVE_FOR_REVIEW_ARTICLE,
							ArticleConstants.CHAR_N,
							articleData.getReviewerid(), null,
							ArticleConstants.ARTICLE_REQUEST_TYPE_NAME,
							articleSeqId);
		}

	}

	public String getLegacyObjectCode(String assetType, String objCode) {

		String query = null;
		if (assetType != null && assetType.equalsIgnoreCase("ARTICLE")) {
			query = DRBQueryConstants.GET_ARTICLE_LEGACY_CODE;
		} else if (assetType != null && assetType.equalsIgnoreCase("FOLDER")) {
			query = DRBQueryConstants.GET_FOLDER_LEGACY_CODE;
		}

		List<String> legacyCodeLs = getSimpleJdbcTemplate().query(query,
				new ParameterizedRowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {

						return rs.getString(1);

					}
				}, objCode);
		if (!DRBUtils.isEmptyList(legacyCodeLs)) {
			return legacyCodeLs.get(0);
		} else {
			return null;
		}
	}

	public List<String> getArticleTaggingComments(String articleSeqId,
			String libId) {
		List<String> comments = new ArrayList<String>();
		List<Map<String, Object>> returnValues = getSimpleJdbcTemplate()
				.queryForList(
						DRBQueryConstants.TAGGER_UPDATED_COMMENTS,
						new Object[] {
								articleSeqId,
								ArticleConstants.REQUEST_TYPE_DRAFT_ARTICLE_TAGGER,
								libId });
		for (Map<String, Object> returnValue : returnValues) {
			comments.add((String) returnValue.get("REQUEST_DESCRIPTION"));
			break;
		}
		return comments;
	}

	/*
	 * Start: Added for Use Case 03: Add Properties field for engine/product
	 * like it is in DR
	 */
	/*
	 * private List<SelectItem> fetchEngineModelList()throws DRBCommonException{
	 * List<SelectItem> engineModel = getSimpleJdbcTemplate().query(
	 * DRBQueryConstants.ENGINE_MODEL_LIST, engineModelListMapper); return
	 * engineModel;
	 * 
	 * }
	 */

	// Added by 307009315 forODA RR : for Engine family list

	private List<SelectItem> fetchFAACodes() throws DRBCommonException {
		List<SelectItem> faaCodes = getSimpleJdbcTemplate().query(
				DRBQueryConstants.FAA_FUNC_CODE, faaMapper);
		return faaCodes;
	}

	private static ParameterizedRowMapper<SelectItem> faaMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.FAA_FILE_SEQID),
					rs.getString(FolderConstants.FAA_FILE_CD) + " - "
							+ rs.getString(FolderConstants.FAA_FILE_CD_DESC));
		}
	};

	private List<SelectItem> fetchRecSources() {
		List<SelectItem> recSource = getSimpleJdbcTemplate().query(
				DRBQueryConstants.REC_SRC, recSrcMapper);
		return recSource;
	}

	private static ParameterizedRowMapper<SelectItem> recSrcMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.REC_SRC_SEQID),
					rs.getString(FolderConstants.REC_SRC_NAME));
		}
	};

	private List<SelectItem> fetchRecTypes() {
		List<SelectItem> recTypes = getSimpleJdbcTemplate().query(
				DRBQueryConstants.REC_TYPE, recTypeMapper);
		return recTypes;
	}

	private static ParameterizedRowMapper<SelectItem> recTypeMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.REC_TYPESEQID),
					rs.getString(FolderConstants.REC_TYPE));
		}
	};

	private List<SelectItem> fetchRSPList() {
		List<SelectItem> rspTypes = getSimpleJdbcTemplate().query(
				DRBQueryConstants.RSP_TYPES, rspMapper);
		return rspTypes;
	}

	private static ParameterizedRowMapper<SelectItem> rspMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.RSP_SEQID),
					rs.getString(FolderConstants.RSP_NAME));
		}
	};

	private List<SelectItem> fetchFAAPjctNums() {
		List<SelectItem> faaPjctCodes = getSimpleJdbcTemplate().query(
				DRBQueryConstants.FAA_PJCT_NUM, faaPjctNumMapper);
		return faaPjctCodes;
	}

	private static ParameterizedRowMapper<SelectItem> faaPjctNumMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.FAA_PJCT_SEQID),
					rs.getString(FolderConstants.FAA_PJCT_NO));
		}
	};

	private List<SelectItem> fetchODAPjctNums() {
		List<SelectItem> odaPjctCodes = getSimpleJdbcTemplate().query(
				DRBQueryConstants.ODA_PJCT_NUM, odaPjctNumMapper);
		return odaPjctCodes;
	}

	private static ParameterizedRowMapper<SelectItem> odaPjctNumMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.FAA_PJCT_SEQID),
					rs.getString(FolderConstants.ODA_PJCT_NO));
		}
	};

	private List<SelectItem> fetchRCENames() {
		List<SelectItem> rceNames = getSimpleJdbcTemplate().query(
				DRBQueryConstants.RCE_RCI_NAMES, rceNameMapper);
		return rceNames;
	}

	private static ParameterizedRowMapper<SelectItem> rceNameMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(
					rs.getString(FolderConstants.RCERCI_USR_SEQID),
					rs.getString(FolderConstants.RCI_OR_RCE_GIVENAME),
					rs.getString(FolderConstants.RCI_OR_RCE_ROLESEQID));
		}
	};

	private List<SelectItem> fetchFaaFuncCodeList() throws DRBCommonException {
		String query = null;
		List<SelectItem> faaOdaFuncCodes = null;
		query = DRBQueryConstants.FAA_ODA_FUNC_CODE;
		faaOdaFuncCodes = getSimpleJdbcTemplate().query(query, faaOdaFuncCode);
		return faaOdaFuncCodes;
	}

	private static ParameterizedRowMapper<SelectItem> faaOdaFuncCode = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(
					rs.getString(FolderConstants.ODA_FAA_FUNC_SEQID),
					rs.getString(FolderConstants.ODA_FAA_DESCR));
		}
	};

	private List<SelectItem> fetchFormNums() throws DRBCommonException {
		String query = null;
		List<SelectItem> forms = null;
		query = DRBQueryConstants.FORM_NUM;
		forms = getSimpleJdbcTemplate().query(query, formNosMapper);
		return forms;
	}

	private static ParameterizedRowMapper<SelectItem> formNosMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.FORMNO_SEQID),
					rs.getString(FolderConstants.FORMNO_DESCR));
		}
	};

	private String getODAManRev() throws DRBCommonException {
		String query = null;
		// int odaRev = 0;
		query = DRBQueryConstants.ODA_MANUAL_REV;
		int odaRev = getSimpleJdbcTemplate()
				.queryForInt(query, new Object[] {});
		LOG.info("Max Manual Revision from ODA table is " + odaRev);
		String odaRevDescr = getSimpleJdbcTemplate().queryForObject(
				DRBQueryConstants.ODA_MAN_REV_DESCR, odaManRevDescr,
				new Object[] { odaRev });
		String manRevWithId = odaRevDescr + ":" + odaRev;
		System.out.println("Revision number with ID " + manRevWithId);
		return manRevWithId;

	}

	private static ParameterizedRowMapper<String> odaManRevDescr = new ParameterizedRowMapper<String>() {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String descr = ArticleConstants.EMPTY;
			descr = (DRBUtils.checkNullVal((String) rs
					.getString(FolderConstants.ODA_MAN_REVDESCR)));
			return descr;
		}
	};

	private String getODAAuthNum() throws DRBCommonException {
		String query = null;

		query = DRBQueryConstants.ODA_AUTH_NUM;
		String odaAuth = getSimpleJdbcTemplate().queryForObject(query,
				authNumDesc, new Object[] {});
		LOG.info("ODA Auth number" + odaAuth);
		return odaAuth;
	}

	private static ParameterizedRowMapper<String> authNumDesc = new ParameterizedRowMapper<String>() {
		public String mapRow(ResultSet rs1, int rowNum) throws SQLException {
			String odaAuthNum = ArticleConstants.EMPTY;
			odaAuthNum = (DRBUtils.checkNullVal((String) rs1
					.getString(FolderConstants.ODA_AUTH_NUM)) + ":" + rs1
					.getInt(FolderConstants.ODA_AUTH_SEQID));
			return odaAuthNum;

		}
	};

	private List<SelectItem> fetchCCLItems() throws DRBCommonException {
		String query = null;
		List<SelectItem> ccls = null;
		query = DRBQueryConstants.CCL_ITEMS;
		ccls = getSimpleJdbcTemplate().query(query, cclMapper);
		return ccls;
	}

	private static ParameterizedRowMapper<SelectItem> cclMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(rs.getString(FolderConstants.CCL_SEQ_ID),
					rs.getString(FolderConstants.CCL_ITM_NO) + "-"
							+ rs.getString(FolderConstants.CCL_ITM_DESCR));
		}
	};

	/*
	 * Commented by 307009315 - 7 Nov 13
	 * 
	 * private List<SelectItem> fetchRCENames() { List<SelectItem> rceNames =
	 * getSimpleJdbcTemplate
	 * ().query(DRBQueryConstants.RCE_RCI_NAMES,rceNameMapper,new
	 * Object[]{"RCE"}); return rceNames; }
	 * 
	 * private static ParameterizedRowMapper<SelectItem> rceNameMapper = new
	 * ParameterizedRowMapper<SelectItem>() { public SelectItem mapRow(ResultSet
	 * rs, int rowNum) throws SQLException { return new
	 * SelectItem(rs.getString(FolderConstants.RCERCI_USR_SEQID),
	 * rs.getString(FolderConstants.RCI_OR_RCE_GIVENAME)+" "+
	 * rs.getString(FolderConstants.RCI_OR_RCE_FAMILYNAME)); } };
	 * 
	 * 
	 * 
	 * private List<SelectItem> fetchATAList() throws DRBCommonException {
	 * String query = null; List<SelectItem> ataList = null; query =
	 * DRBQueryConstants.ATA_NUMS; ataList =
	 * getSimpleJdbcTemplate().query(query,ataListMapper); return ataList; }
	 * 
	 * private static ParameterizedRowMapper<SelectItem> ataListMapper = new
	 * ParameterizedRowMapper<SelectItem>() { public SelectItem mapRow(ResultSet
	 * rs, int rowNum) throws SQLException { return new
	 * SelectItem(rs.getString(FolderConstants.ATA_SEQID),
	 * rs.getString(FolderConstants.ATA_DESCR)); } };
	 * 
	 * private List<SelectItem> fetchEngFlyList() throws DRBCommonException {
	 * String query = null; List<SelectItem> engFly = null; query =
	 * DRBQueryConstants.ENG_FLY_LIST; engFly =
	 * getSimpleJdbcTemplate().query(query,engFamily);
	 * 
	 * return engFly; }
	 * 
	 * private static ParameterizedRowMapper<SelectItem> engFamily = new
	 * ParameterizedRowMapper<SelectItem>() { public SelectItem mapRow(ResultSet
	 * rs, int rowNum) throws SQLException { return new
	 * SelectItem(rs.getString(FolderConstants.ENG_FAMILY_SEQ_ID),
	 * rs.getString(FolderConstants.ENGFAMILY)); } }; public synchronized
	 * List<SystemList> getEngineSubModelsByIds(List<String> ids){
	 * 
	 * try{ String[] ss=new String[ids.size()]; Object[] ob =ids.toArray();
	 * String idsList=""; StringBuffer idsl=new StringBuffer(); for(int
	 * i=0;i<ob.length;i++){ ss[i]=ob[i].toString();
	 * idsl.append(ob[i]).append(","); }
	 * 
	 * List<SystemList> list=null; idsList=idsl.toString();
	 * idsList=idsList.substring(0, idsList.lastIndexOf(',')); String query =
	 * "select ENG_SUB_SEQ_ID,MODEL_SUB_DESCR from CAWC.awc_eng_sub where eng_sub_seq_id in ("
	 * +idsList + ')';
	 * 
	 * 
	 * ParameterizedRowMapper<SystemList> engSubMapper=new
	 * ParameterizedRowMapper<SystemList>() {
	 * 
	 * public SystemList mapRow(ResultSet rs, int arg1) throws SQLException {
	 * SystemList item=new
	 * SystemList(rs.getInt("ENG_SUB_SEQ_ID"),rs.getString("MODEL_SUB_DESCR"));
	 * return item; } }; list=getSimpleJdbcTemplate().query(query,engSubMapper);
	 * return list; }catch(Exception e){
	 * LOG.error("Exception caught in getEngineSubModelsByIds()");
	 * e.printStackTrace(); return null;
	 * 
	 * } }
	 * 
	 * public List<SystemList> getEngineModelsByFamily(int familyId){ String
	 * query = null; //ArticleData articleData = new ArticleData();
	 * List<SystemList> engMdlList = null; query =
	 * DRBQueryConstants.ENG_MDL_LIST; engMdlList =
	 * getSimpleJdbcTemplate().query(query,engMdl,new Object[]{familyId});
	 * 
	 * System.out.println("Family -eng model size :"+engMdlList.size()); return
	 * engMdlList; }
	 * 
	 * private static ParameterizedRowMapper<SystemList> engMdl = new
	 * ParameterizedRowMapper<SystemList>() { public SystemList mapRow(ResultSet
	 * resultSet, int rowNum) throws SQLException { SystemList srData = new
	 * SystemList(); srData.setId(resultSet.getInt("ENG_MODEL_SEQ_ID"));
	 * srData.setDescription(resultSet.getString("ENG_DESCR")); return srData; }
	 * };
	 * 
	 * public List<SystemList> getEngineSubModelsbyModel(int modelId){ String
	 * query = null; List<SystemList> engSubMdlList = null; query =
	 * DRBQueryConstants.ENG_SUBMDL_LIST; engSubMdlList =
	 * getSimpleJdbcTemplate().query(query,engSubMdls,new Object[]{modelId});
	 * System.out.println("Family -eng sub model size :"+engSubMdlList.size());
	 * 
	 * return engSubMdlList; } private static ParameterizedRowMapper<SystemList>
	 * engSubMdls = new ParameterizedRowMapper<SystemList>() { public SystemList
	 * mapRow(ResultSet resultSet, int rowNum) throws SQLException { SystemList
	 * srData = new SystemList();
	 * srData.setId(resultSet.getInt("ENG_SUB_SEQ_ID"));
	 * srData.setDescription(resultSet.getString("MODEL_SUB_DESCR")); return
	 * srData; } };
	 */

	// ATTACH_TYPE

	public List<SelectItem> fetchAttachments() throws DRBCommonException {
		List<SelectItem> attachmnts = getSimpleJdbcTemplate().query(
				DRBQueryConstants.ATTACH_TYPE, attachmentTypesMapper);

		return attachmnts;

	}

	private static ParameterizedRowMapper<SelectItem> attachmentTypesMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(
					rs.getString(FolderConstants.ATTACHEMT_SEQID),
					rs.getString(FolderConstants.ATTACMENT_DESCR));
		}
	};

	// Added ends

	private List<SelectItem> fetchReviewAreaList() throws DRBCommonException {
		List<SelectItem> reviewArea = getSimpleJdbcTemplate().query(
				DRBQueryConstants.REVIEW_AREA_LIST, reviewAreaListMapper);
		return reviewArea;

	}

	private List<SelectItem> fetchDisciplineAreaList()
			throws DRBCommonException {
		List<SelectItem> engineModel = getSimpleJdbcTemplate().query(
				DRBQueryConstants.DISCIPLINE_AREA_LIST,
				disciplineAreaListMapper);
		return engineModel;

	}

	/*
	 * private static ParameterizedRowMapper<SelectItem> engineModelListMapper =
	 * new ParameterizedRowMapper<SelectItem>() { public SelectItem
	 * mapRow(ResultSet rs, int rowNum) throws SQLException { return new
	 * SelectItem(rs .getString(FolderConstants.ENGINE_MODEL_SEQ_ID),rs
	 * .getString(FolderConstants.ENGINE_MODEL_NAME)); } };
	 */

	private static ParameterizedRowMapper<SelectItem> reviewAreaListMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(
					rs.getString(FolderConstants.REVIEW_AREA_SEQ_ID),
					rs.getString(FolderConstants.REVIEW_AREA_NAME));
		}
	};

	private static ParameterizedRowMapper<SelectItem> disciplineAreaListMapper = new ParameterizedRowMapper<SelectItem>() {
		public SelectItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new SelectItem(
					rs.getString(FolderConstants.DISCIPLINE_AREA_SEQ_ID),
					rs.getString(FolderConstants.DISCIPLINE_AREA_NAME));
		}
	};

	private ArticleData getPropertyList(ArticleData articleData)
			throws DRBCommonException {

		List<SelectItem> reviewAreaList = null;
		List<SelectItem> disciplineAreaList = null;
		// Added by 307009315 for ODA RR
		List<SelectItem> faaFuncCodes = null;
		List<SelectItem> odaPjctNos = null;
		List<SelectItem> faaPjctNos = null;
		List<SelectItem> recTypes = null;
		List<SelectItem> recSources = null;
		List<SelectItem> rspTypes = null;
		List<SelectItem> cclItems = null;
		List<SelectItem> rceNames = null;
		List<SelectItem> faaOdaFuncCodes = null;
		List<SelectItem> formNos = null;
		String odaManRev = null;
		String odaAuthNum = null;
		List<SelectItem> attTypes = null;

		faaFuncCodes = fetchFAACodes();
		odaPjctNos = fetchODAPjctNums();
		faaPjctNos = fetchFAAPjctNums();
		recTypes = fetchRecTypes();
		recSources = fetchRecSources();
		rspTypes = fetchRSPList();
		cclItems = fetchCCLItems();
		reviewAreaList = fetchReviewAreaList();
		disciplineAreaList = fetchDisciplineAreaList();

		// rciNames = fetchRCINames();
		rceNames = fetchRCENames();
		faaOdaFuncCodes = fetchFaaFuncCodeList();
		formNos = fetchFormNums();

		odaManRev = getODAManRev();
		odaAuthNum = getODAAuthNum();
		attTypes = fetchAttachments();

		// engModelList = fetchEngMdlList();
		articleData.setFaaFileCodes(faaFuncCodes);
		articleData.setReviewAreaList(reviewAreaList);
		articleData.setDisciplineAreaList(disciplineAreaList);
		articleData.setFAAPjctCodes(faaPjctNos);
		articleData.setODAPjctCodes(odaPjctNos);
		articleData.setRecTypes(recTypes);
		articleData.setRspList(rspTypes);
		articleData.setRecSource(recSources);
		// articleData.setRciNames(rciNames);
		articleData.setRceNames(rceNames);
		articleData.setFaaFuncCodes(faaOdaFuncCodes);
		articleData.setFormNumbers(formNos);
		articleData.setAttachTypes(attTypes);
		articleData.setCclItms(cclItems);

		// Split oda Man Revision
		String[] odaId = odaManRev.split(":");
		articleData.setOdaManualRevNo(odaId[0]);
		articleData.setOdaManRevId(Integer.parseInt(odaId[1]));

		System.out.println("Rev manual no :" + articleData.getOdaManualRevNo());
		System.out.println("Rev manual ID :" + articleData.getOdaManRevId());

		String[] odaAuthNumber = odaAuthNum.split(":");
		articleData.setOdaAuthNum(odaAuthNumber[0]);
		articleData.setOdaAuthId(Integer.parseInt(odaAuthNumber[1]));

		// articleData.setOdaManualRevNo(odaManRev);
		// articleData.setOdaAuthNum(odaAuthNum);

		// Commented by 307009315 - 7 Nov 13
		/*
		 * cclItemList = fetchCCLItems();
		 * articleData.setEngModelList(engModelList);
		 * articleData.setEngFamily(engineFlyList);
		 * articleData.setAtaNums(ataList); ataList = fetchATAList();
		 * engineModelList = fetchEngineModelList(); engineFlyList =
		 * fetchEngFlyList();
		 * 
		 * articleData.setCclItms(cclItms);
		 */
		return articleData;
	}

	/*
	 * End: Added for Use Case 03: Add Properties field for engine/product like
	 * it is in DR
	 */

	/* Start : Added for Use Case 23: Stream line the Article tagging process */
	public void updateTaskForTagger(String taggerId, String librarySelected,
			String articleFolderSeqNo, String status) throws DRBCommonException {

		String reqTypeFrom = null;
		String reqTypeTo = null;
		String statusFrom = null;
		int taskSeqId = 0;
		// String returnStr = ArticleConstants.STATE_FAILURE;
		if (status.equalsIgnoreCase(ArticleConstants.CHAR_Y)) {
			reqTypeFrom = ArticleConstants.REQUEST_TYPE_DRAFT_ARTICLE_TAGGER;
			reqTypeTo = ArticleConstants.STRING_GET_ARTICLE_EC_ACCESS;
			statusFrom = ArticleConstants.CHAR_N;

		} else {
			reqTypeFrom = ArticleConstants.STRING_GET_ARTICLE_EC_ACCESS;
			reqTypeTo = ArticleConstants.REQUEST_TYPE_DRAFT_ARTICLE_TAGGER;
			statusFrom = ArticleConstants.CHAR_Y;
		}

		Object[] argsForSeq = { articleFolderSeqNo, statusFrom, taggerId,
				reqTypeFrom, librarySelected };
		taskSeqId = getSimpleJdbcTemplate().queryForInt(
				DRBQueryConstants.TASK_FOR_TAGGER_SEQ_ID, argsForSeq);

		if (taskSeqId != 0) {
			Object[] args = { status, reqTypeTo, taskSeqId };
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_TASK_FOR_TAGGER, args);
			// String returnStr=ArticleConstants.STATE_SUCCESS;
		}
	}

	public void deleteTaskfromOldTaggerDraft(String oldTaggerId,
			String articleSeqId) throws DRBCommonException {
		Object[] args = { articleSeqId, oldTaggerId,
				ArticleConstants.REQUEST_TYPE_DRAFT_ARTICLE_TAGGER };
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.DELETE_OLD_TAGGER_ARTICLE_DRAFT, args);

	}

	/* End : Added for Use Case 23: Stream line the Article tagging process */

	/** Start: Added for DRBA Oct 2012 Release Compliance Enhancement **/

	@Transactional
	public boolean updateArticleOwner(String oldArticleOwnerSelected,
			String newArticleOwnerSelected, String articleOwnerType,
			String libSeqId, String loginUser) throws DRBCommonException {
		boolean status = false;
		List<ArticleData> articleDataList = new ArrayList<ArticleData>();
		if (LibraryConstants.INACTIVE_ARTICLE_OWNERS
				.equalsIgnoreCase(articleOwnerType)) {
			articleDataList = getSimpleJdbcTemplate().query(
					DRBQueryConstants.GET_ARTICLE_NUMBER_LIST,
					inactiveArticleDataList, oldArticleOwnerSelected, libSeqId);

			int articleDataSize = articleDataList.size();
			int count = 0;
			StringBuffer buf = new StringBuffer();

			String articleIds = null;
			if (articleDataSize > 0) {
				articleIds = DRBQueryConstants.OPENING_BRACKET;

				for (ArticleData ad : articleDataList) {
					// Modified for PERFORMANCE issue fix in nimble report
					buf.append("'").append(ad.getArticleSeqid()).append("'");

					articleIds = articleIds + "'" + ad.getArticleSeqid() + "'";
					count++;
					if (count != articleDataSize) {
						articleIds += ", ";
					}
				}

				articleIds += DRBQueryConstants.CLOSING_BRACKET;

				StringBuffer updateArticleQuery = new StringBuffer(
						DRBQueryConstants.UPDATE_INACTIVE_ARTICLE_OWNER);
				updateArticleQuery.append(articleIds);

				// int updatedArticlesCount =
				getSimpleJdbcTemplate().update(updateArticleQuery.toString(),
						new Object[] { newArticleOwnerSelected, libSeqId });

				StringBuffer insertAppDataHistoryQuery = new StringBuffer(
						DRBQueryConstants.INSERT_INACTIVE_APP_DATA_HISTORY);
				insertAppDataHistoryQuery.append(articleIds);

				// int insertAppDataCount =
				getSimpleJdbcTemplate()
						.update(insertAppDataHistoryQuery.toString(),
								new Object[] { oldArticleOwnerSelected,
										oldArticleOwnerSelected,
										newArticleOwnerSelected, loginUser,
										loginUser });

			}

		}

		return status;
	}

	private static ParameterizedRowMapper<ArticleData> inactiveArticleDataList = new ParameterizedRowMapper<ArticleData>() {
		public ArticleData mapRow(ResultSet rs, int rowNum) throws SQLException {
			ArticleData metaData = new ArticleData();
			metaData.setArticleSeqid(rs.getString("ARTICLE_SEQ_ID"));
			metaData.setArticleCode(rs.getString("ARTICLE_CODE"));
			return metaData;
		}
	};

	public List<FolderMetaData> getCloneObjectForAccess(String articleCode,
			String userId) throws DRBCommonException {
		List<FolderMetaData> accessLst = null;
		/*
		 * List<FolderMetaData> finalAccessLst = null; finalAccessLst = new
		 * ArrayList<FolderMetaData>();
		 */
		String newArticleCode = "%" + articleCode + "%";
		accessLst = getSimpleJdbcTemplate().query(
				DRBQueryConstants.GET_ARTICLE_CLONE_OBJECT_FOR_ACCESS,
				cloneObjectAccessMapper,
				new Object[] { userId, userId, userId, newArticleCode });
		return accessLst;
	}

	private static ParameterizedRowMapper<FolderMetaData> cloneObjectAccessMapper = new ParameterizedRowMapper<FolderMetaData>() {
		public FolderMetaData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			FolderMetaData metaData = new FolderMetaData();
			metaData.setFolderCode(rs.getString(FolderConstants.ARTICLE_CODE));
			metaData.setFolderTitle(rs.getString(FolderConstants.ARTICLE_TITLE));
			metaData.setFolderPath(rs.getString(FolderConstants.ARTICLE_PATH));
			metaData.setArticleFolderSeqId(rs
					.getString(FolderConstants.ARTICLE_SEQ_ID));
			metaData.setAssetType(rs.getString(FolderConstants.ASSET_TYPE));
			metaData.setArticleStateName(rs
					.getString(FolderConstants.STATE_NAME)); // Added By Santosh
																// on 12JAN11
			metaData.setLockInd(rs.getString(FolderConstants.LOCK_IND));// Added
																		// by
																		// Pradeep
																		// on
																		// 24/01/2011
																		// for
																		// Defect
																		// Id:
																		// 1453
			metaData.setContentInd(rs.getString(FolderConstants.CONTENT_IND));// Added
																				// by
																				// Pradeep
																				// on
																				// 24/01/2011
																				// for
																				// Defect
																				// Id:
																				// 1453
			metaData.setObjectEcTag(rs
					.getString(ReportConstants.ARTICLE_OBJ_EC_TAG));
			metaData.setObjectDocumentClass(rs
					.getString(ReportConstants.ARTICLE_DOC_CLASS));
			return metaData;
		}
	};

	/** End: Added for DRBA Oct 2012 Release Compliance Enhancement **/
	public ArticleData getArticleCloneObject(String cloneObjectCode,
			ArticleData theData) throws DRBCommonException {
		// getSimpleJdbcTemplate().query(DRBQueryConstants.GET_ARTICLE_NUMBER_LIS

		List<ArticleData> theLs = getSimpleJdbcTemplate().query(
				DRBQueryConstants.GET_ARTICLE_CLONE_OBJECT, cloneArticleMapper,
				new Object[] { cloneObjectCode });
		ArticleData data = null;
		if (theLs != null && !DRBUtils.isEmptyList(theLs)) {

			data = theLs.get(0);
		}
		return data;
	}

	private static ParameterizedRowMapper<ArticleData> cloneArticleMapper = new ParameterizedRowMapper<ArticleData>() {
		public ArticleData mapRow(ResultSet rs, int rowNum) throws SQLException {
			ArticleData metaData = new ArticleData();
			metaData.setArticleSeqId(rs.getInt("ARTICLE_SEQ_ID"));
			metaData.setArticleCode(rs.getString("ARTICLE_CODE"));
			metaData.setArticleTitle(DRBUtils.checkNullVal(rs
					.getString("ARTICLE_TITLE")));
			metaData.setKeyWords(DRBUtils.checkNullVal(rs
					.getString("KEY_WORDS")));
			metaData.setArticleSubject(DRBUtils.checkNullVal(rs
					.getString("ARTICLE_SUBJECT")));
			metaData.setArticleAbstract(DRBUtils.checkNullVal(rs
					.getString("ARTICLE_ABSTRACT")));
			metaData.setProblemOrPurpose(DRBUtils.checkNullVal(rs
					.getString("PROBLEM_PURPOSE")));
			metaData.setReferences(DRBUtils.checkNullVal(rs
					.getString("REFERENCES")));
			metaData.setAssumptions(DRBUtils.checkNullVal(rs
					.getString("ASSUMPTIONS")));
			metaData.setResults(DRBUtils.checkNullVal(rs.getString("RESULTS")));
			metaData.setConclusions(DRBUtils.checkNullVal(rs
					.getString("CONCLUSIONS")));
			return metaData;
		}
	};

	public int updateArticleEdited(String articleSeqId, String editValue)
			throws DRBCommonException {
		int res = 0;

		String query = DRBQueryConstants.UPDATE_ARTICLE_EDIT_STATE_WOMD_EDIT;
		res = getSimpleJdbcTemplate().update(query, editValue, articleSeqId);

		return res;
	}

	public int saveAttachmentType(HashMap theAttaType)
			throws DRBCommonException {

		int res = 0;
		if (theAttaType != null) {

			// Set s = theAttaType.entrySet();
			Iterator it = theAttaType.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				int key = ((Integer) pairs.getKey()).intValue();
				String value = (String) pairs.getValue();
				it.remove();
				String query = DRBQueryConstants.UPDATE_ATTACH_TYPE;
				res = getSimpleJdbcTemplate().update(query, value, key);
			}
		}

		return res;
	}

	/**
	 * Save Engine Sub Models
	 */
	public void saveSubEngineModels(final ArticleData articleData)
			throws DRBCommonException {

		final String articleSeqId = articleData.getArticleSeqid();

		try {
			getJdbcTemplate().batchUpdate(
					DRBQueryConstants.INSERT_ENGINE_SUB_MODELS,
					new BatchPreparedStatementSetter() {
						public void setValues(PreparedStatement ps, int i)
								throws SQLException {
							Long engineSubModel = articleData
									.getEngineSubModels().get(i);
							ps.setLong(1, engineSubModel);
							ps.setString(2, articleSeqId);
						}

						public int getBatchSize() {
							return articleData.getEngineSubModels().size();
						}
					});
			this.updateSourceDocId(articleData);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateSourceDocId(ArticleData articleData) {

		try {
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_CAWC_DOC_SEQ_ID,
					articleData.getDocSeqId(), articleData.getArticleSeqid());
		} catch (DataAccessException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Update Engine Sub Models
	 */
	public void updateSubEngineModels(ArticleData articleData)
			throws DRBCommonException {
		try {
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.DELETE_ENGINE_SUB_MODELS,
					articleData.getArticleSeqid());
			saveSubEngineModels(articleData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dependency Injection
	 */
	public void setViewArticleProc(ViewArticleProc viewArticleProc) {
		this.viewArticleProc = viewArticleProc;
	}

	/**
	 * Fetch Record State
	 */
	public String getCAWCRecordState() throws MappingException {
		String state = null;
		try {
			state = getSimpleJdbcTemplate().queryForObject(
					DRBQueryConstants.FETCH_CAWC_RECORD_STATE, String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (state == null) {
			throw new MappingException();
		}
		return state;
	}

	/**
	 * Fetch Record State
	 */
	public String getECMRecordState() throws MappingException {
		String state = null;
		try {
			state = getSimpleJdbcTemplate().queryForObject(
					DRBQueryConstants.FETCH_ECM_RECORD_STATE, String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (state == null) {
			throw new MappingException();
		}
		return state;
	}

	/**
	 * Update Issued date
	 */
	public void updateIssuedDate(ArticleData articleData)
			throws DRBCommonException {
		try {
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_ISSUED_DATE,
					articleData.getArticleSeqid());
			getSimpleJdbcTemplate().update(
					DRBQueryConstants.UPDATE_HISTORY_ISSUED,
					articleData.getArticleSeqid());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * check whether article already exists
	 */
	public List<ArticleData> isArticleExist(String documentNumbe,
			String libraryId, String source) throws DRBCommonException {
		List<ArticleData> articles = null;
		try {
			articles = getSimpleJdbcTemplate().query(
					DRBQueryConstants.FETECH_EXISTING_RECORDS, FETCH_ARTICLES,
					documentNumbe, source, libraryId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return articles;
	}

	private static ParameterizedRowMapper<ArticleData> FETCH_ARTICLES = new ParameterizedRowMapper<ArticleData>() {
		public ArticleData mapRow(ResultSet rs, int rowNum) throws SQLException {
			ArticleData data = new ArticleData();
			data.setArticleSeqid(rs
					.getString(ODARRCAWCConstants.ARTICLE_SEQ_ID));
			data.setArticleCode(rs.getString(ODARRCAWCConstants.ARTICLE_CODE));
			data.setArticleNumber(rs.getString(ODARRCAWCConstants.ARTICLE_CODE));
			data.setArticleState(rs.getString(ODARRCAWCConstants.STATE_NAME));
			data.setFolderSeqId(rs.getString(ODARRCAWCConstants.FOLDER_SEQ_ID));
			data.setArticlePath(rs.getString(ODARRCAWCConstants.ARTICLE_PATH));
			data.setArticleTitle(rs.getString(ODARRCAWCConstants.ARTICLE_TITLE));
			data.setArticleOwnerSSO(rs
					.getString(ODARRCAWCConstants.OWNER_USER_ID));
			return data;
		}
	};

	/*
	 * create Revision
	 */
	public ArticleData createRevision(ArticleData articleData,
			List<String> toSaveUpdate, String emailId)
			throws DRBCommonException {

		String prevArtSeqId = null;
		List<String> emailIds = new ArrayList<String>();
		articleData.setButtonName(null);
		articleData.setArticleReviseInd(false);
		prevArtSeqId = articleData.getArticleSeqid();
		Map out = new HashMap();

		String temparticleIsPaper = "";
		try {
			if (articleData.getArticleIsPaper() != null
					&& "Yes".equalsIgnoreCase(articleData.getArticleIsPaper())) {
				temparticleIsPaper = "Y";
			} else {
				temparticleIsPaper = "N";
			}
			/**
			 * Start: Modified by Kanoj for Enhancement No: 147
			 */
			out = _createArticlePrc.executeReviseArticle(
					Integer.parseInt(articleData.getArticleSeqid()),
					articleData.getArticleTitle(),
					articleData.getArticleOldTitle(),
					articleData.getArticleSubject(),
					articleData.getArticleOwnerSSO(),
					articleData.getSecOwnerId(),
					Integer.parseInt(articleData.getFolderSeqId()),
					articleData.getKeyWords(),
					articleData.getOwningBusinessSegment(),
					articleData.getArticleAbstract(), temparticleIsPaper/*
																		 * articleData
																		 * .
																		 * getArticleIsPaper
																		 * ()
																		 */,
					articleData.getExportControlTag(),
					Integer.parseInt(articleData.getGeDocumentClassNumber()),
					articleData.getReferences(), articleData.getIpIssue(),
					articleData.getIpTag(), articleData.getProblemOrPurpose(),
					articleData.getAssumptions(), articleData.getResults(),
					articleData.getConclusions(),
					articleData.getLegacyArticleNumber(),
					articleData.getArticleAuthors(),
					articleData.getArticleOwner(),
					articleData.getArticleNumber(), articleData.getDrNumber(),
					articleData.getAiNumber(), articleData.getEngineModelId(),
					articleData.getReviewAreaId(),
					articleData.getDisciplineAreaId(),
					articleData.getArtErrortype());
			/**
			 * End: Modified by Kanoj for Enhancement No: 147
			 */

			articleData.setArticleSeqid((String) out.get("articleSeqIdOut"));
			System.out.println("Revision Article seq id:"
					+ articleData.getArticleSeqid());

			String projectSeqId = null;
			if (articleData.getOdaPjctNosId() != null) {
				projectSeqId = articleData.getOdaPjctNosId();
			} else if (articleData.getFaaPjctIdList() != null) {
				projectSeqId = articleData.getFaaPjctIdList();
			}
			if (articleData.getFaaFileCodeIdList() == null
					|| "".equalsIgnoreCase(articleData.getFaaFileCodeIdList())) {
				articleData.setFaaFileCodeIdList("0");
			}
			if (articleData.getRceNamesIdList() == null
					|| "".equalsIgnoreCase(articleData.getRceNamesIdList())) {
				articleData.setRceNamesIdList("0");
			}
			if (articleData.getRspIdList() == null
					|| "".equalsIgnoreCase(articleData.getRspIdList())) {
				articleData.setRspIdList("0");
			}
			if (articleData.getFormNoIdList() == null
					|| "".equalsIgnoreCase(articleData.getFormNoIdList())) {
				articleData.setFormNoIdList("0");
			}
			if (articleData.getRecTypesIdList() == null
					|| "".equalsIgnoreCase(articleData.getRecTypesIdList())) {
				articleData.setRecTypesIdList("0");
			}
			if (articleData.getFaaOdaFuncIdList() == null
					|| "".equalsIgnoreCase(articleData.getFaaOdaFuncIdList())) {
				articleData.setFaaOdaFuncIdList("0");
			}
			if (articleData.getFormNoIdList() == null
					|| "".equalsIgnoreCase(articleData.getFormNoIdList())) {
				articleData.setFormNoIdList("0");
			}
			if (articleData.getRoleSeqId() == null
					|| "".equalsIgnoreCase(articleData.getRoleSeqId())) {
				articleData.setRoleSeqId("0");
			}

			/*
			 * LOG.info("Before new insert query call articleSeqid..."+articleData
			 * .getArticleSeqid());
			 * LOG.info("Before new insert query call faaFileCode..."
			 * +articleData.getFaaFileCodeIdList());
			 * LOG.info("Before new insert query call oda pjct no..."
			 * +articleData.getOdaPjctNosId());
			 * LOG.info("Before new insert query call rec type..."
			 * +articleData.getRecTypesIdList());
			 * LOG.info("Before new insert query call rec source id..."
			 * +articleData.getRecSourceIdList());
			 * LOG.info("Before new insert query call rce names id..."
			 * +articleData.getRceNamesIdList());
			 * LOG.info("Before new insert query call rsp id..."
			 * +articleData.getRspIdList());
			 * LOG.info("Before new insert query call form no..."
			 * +articleData.getFormNoIdList());
			 * LOG.info("Before new insert query call owner SSO..."
			 * +articleData.getArticleOwnerSSO());
			 * LOG.info("Before new insert query call Cawc Doc no..."
			 * +articleData.getCawcDocNum());
			 * LOG.info("Before new insert query call CCL items..."
			 * +articleData.getCclIdList());
			 * LOG.info("Before new insert query call FAA Function code..."
			 * +articleData.getFaaOdaFuncIdList());
			 */

			getSimpleJdbcTemplate().update(
					DRBQueryConstants.INSERT_ODARR_NEWFIELDS,
					Integer.parseInt(articleData.getArticleSeqid()),
					Integer.parseInt(articleData.getFaaFileCodeIdList()),
					Integer.parseInt(articleData.getRecTypesIdList()),
					Integer.parseInt(projectSeqId)// Can be either FAA Project
													// Number or ODA Project
													// Number
					,
					Integer.parseInt(articleData.getRecSourceIdList()),
					articleData.getCawcDocNum(),
					Integer.parseInt(articleData.getRceNamesIdList()),
					Integer.parseInt(articleData.getRoleSeqId()),// Added for
																	// testing
																	// purpse.change
																	// laetr
					Integer.parseInt(articleData.getRspIdList()),
					Integer.parseInt(articleData.getFaaOdaFuncIdList()),
					articleData.getCclIdList(),
					// articleData.getCclItms(),
					Integer.parseInt(articleData.getFormNoIdList()),
					articleData.getOdaManRevId(),// ODA_MAN_REV_SEQ_ID
					articleData.getOdaAuthId(),// ODA_AUTH_SEQ_ID
					articleData.getArticleOwnerSSO(),
					articleData.getArticleOwnerSSO(),
					Integer.parseInt(articleData.getArticleSeqid()));

			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.UPDATE_CHECKED_OUT_ARTICLE_STATE_TO_ISSUED,
							prevArtSeqId);

			emailIds.add(emailId);
			if (emailIds != null && emailIds.size() > 0) {
				this.versionChangeEmail(prevArtSeqId, emailIds,
						articleData.getArticleState(), articleData);
			}

			Map saveout = new HashMap();
			saveout = viewArticleProc.executeViewArticle(
					Integer.parseInt(articleData.getArticleSeqid()),
					articleData.getArticleOwnerSSO(), "ALL",
					Integer.parseInt(articleData.getFolderSeqId()),
					articleData, "false");
			articleData = (ArticleData) saveout.get("articleData");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return articleData;
	}

	/**
	 * Update Article
	 * 
	 */
	public void UpdateArticle(UserData userData, ArticleData articleData)
			throws DRBCommonException {

		try {
			String userName = userData.getUserLastName()
					+ ArticleConstants.EDRB_SEPARATOR
					+ userData.getUserFirstName();
			String query = DRBQueryConstants.ARTICLE_CREATE_ARTICLE_SAVE_ARTICLE_QUERY;
			String odaUpdateQuery = DRBQueryConstants.ODARR_RECORD_UPDATE_QUERY;
			String newdataClassName = ArticleConstants.EMPTY;
			String oldDataClassName = ArticleConstants.EMPTY;

			System.out.println("SEQ ID:" + articleData.getArticleSeqid());

			if (!DRBUtils.isNullOrEmpty(articleData.getGeDocumentClassNumber())) {
				newdataClassName = getSimpleJdbcTemplate()
						.queryForObject(
								DRBQueryConstants.SELECT_DATA_CLASSIFICATION_NAME,
								docClassifcationMapper,
								new Object[] { articleData
										.getGeDocumentClassNumber() });
				oldDataClassName = getSimpleJdbcTemplate().queryForObject(
						DRBQueryConstants.DATA_CLASS_ARTICLE,
						docClassifcationMapper,
						new Object[] { articleData.getArticleSeqid() });
			}
			// LOG.info("Before Update Query================"+articleData.getGeneralpool()
			// );
			// Added By Naresh for GENERALPOOL ISSUE Start
			if (articleData.getGeneralpool() != null) {
				if ("Yes".equalsIgnoreCase(articleData.getGeneralpool())) {
					articleData.setGeneralpool("Y");
				} else if ("No".equalsIgnoreCase(articleData.getGeneralpool())) {
					articleData.setGeneralpool("N");
				}
			} else {
				articleData.setGeneralpool("");
			}
			// Added By Naresh for GENERALPOOL ISSUE End
			String temparticleIsPaper = "";
			if (articleData.getArticleIsPaper() != null
					&& "Yes".equalsIgnoreCase(articleData.getArticleIsPaper())) {
				temparticleIsPaper = "Y";
			} else {
				temparticleIsPaper = "N";
			}

			getSimpleJdbcTemplate().update(
					query,
					new Object[] {

							articleData.getArticleTitle(),
							articleData.getArticleSubject(),
							articleData.getKeyWords(),
							Integer.parseInt(articleData
									.getOwningBusinessSegment()),
							temparticleIsPaper
							/* articleData.getArticleIsPaper() */,
							articleData.getArticleAbstract(),
							Integer.parseInt(articleData
									.getGeDocumentClassNumber()),
							articleData.getReferences(),
							articleData.getIpIssue(),
							articleData.getIpTag(),
							articleData.getProblemOrPurpose(),
							articleData.getAssumptions(),
							articleData.getResults(),
							articleData.getConclusions(),
							articleData.getArticleAuthors(),
							articleData.getSecOwnerId(),
							articleData.getLegacyArticleNumber(),
							userName,
							articleData.getGeneralpool(),
							articleData.getDrNumber(),
							articleData.getAiNumber(),
							// Added for Use Case 03: Add Properties field for
							// engine/product like it is in DR
							articleData.getEngineModelId(),
							articleData.getReviewAreaId(),
							articleData.getDisciplineAreaId(),
							articleData.getArtErrortype(),
							Integer.parseInt(articleData.getArticleSeqid()) });

			String projectSeqId = null;
			if (articleData.getOdaPjctNosId() != null) {
				projectSeqId = articleData.getOdaPjctNosId();
			} else if (articleData.getFaaPjctIdList() != null) {
				projectSeqId = articleData.getFaaPjctIdList();
			}
			if (articleData.getFaaFileCodeIdList() == null
					|| "".equalsIgnoreCase(articleData.getFaaFileCodeIdList())) {
				articleData.setFaaFileCodeIdList("0");
			}
			if (articleData.getRceNamesIdList() == null
					|| "".equalsIgnoreCase(articleData.getRceNamesIdList())) {
				articleData.setRceNamesIdList("0");
			}
			if (articleData.getRspIdList() == null
					|| "".equalsIgnoreCase(articleData.getRspIdList())) {
				articleData.setRspIdList("0");
			}
			if (articleData.getFormNoIdList() == null
					|| "".equalsIgnoreCase(articleData.getFormNoIdList())) {
				articleData.setFormNoIdList("0");
			}
			if (articleData.getRecTypesIdList() == null
					|| "".equalsIgnoreCase(articleData.getRecTypesIdList())) {
				articleData.setRecTypesIdList("0");
			}
			if (articleData.getFaaOdaFuncIdList() == null
					|| "".equalsIgnoreCase(articleData.getFaaOdaFuncIdList())) {
				articleData.setFaaOdaFuncIdList("0");
			}
			if (articleData.getFormNoIdList() == null
					|| "".equalsIgnoreCase(articleData.getFormNoIdList())) {
				articleData.setFormNoIdList("0");
			}
			if (articleData.getRoleSeqId() == null
					|| "".equalsIgnoreCase(articleData.getRoleSeqId())) {
				articleData.setRoleSeqId("0");
			}

			getSimpleJdbcTemplate()
					.update(odaUpdateQuery,
							new Object[] {
									Integer.parseInt(articleData
											.getFaaFileCodeIdList()),
									Integer.parseInt(articleData
											.getRecTypesIdList()),
									Integer.parseInt(projectSeqId),
									Integer.parseInt(articleData
											.getRecSourceIdList()),
									articleData.getCawcDocNum(),
									Integer.parseInt(articleData
											.getRceNamesIdList()),
									Integer.parseInt(articleData.getRoleSeqId()),
									Integer.parseInt(articleData.getRspIdList()),
									Integer.parseInt(articleData
											.getFaaOdaFuncIdList()),
									articleData.getCclIdList(),
									Integer.parseInt(articleData
											.getFormNoIdList()),
									articleData.getOdaManRevId(),
									articleData.getOdaAuthId(),
									articleData.getArticleOwnerSSO(),
									Integer.parseInt(articleData
											.getArticleSeqid()),
									Integer.parseInt(articleData
											.getArticleSeqid()) });

			if (!oldDataClassName.equalsIgnoreCase(newdataClassName)) {
				articleData.setOldValue(oldDataClassName);
				articleData.setNewValue(newdataClassName);
				articleData.setChangedAttributeName("DocumentClassification");
				articleData.setArticleSeqId(Integer.parseInt(articleData
						.getArticleSeqid()));
				captureArticlesHistory(articleData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update Article state to Issued
	 */
	public ArticleData promoteArticleToIssuedState(ArticleData articleData,
			UserData ownerUserObj) throws DRBCommonException {

		List<String> emailIds = null;
		String userName = ownerUserObj.getUserLastName()
				+ ArticleConstants.EDRB_SEPARATOR
				+ ownerUserObj.getUserFirstName();

		if (articleData.getArticleState().equalsIgnoreCase("Issued")) {

			auditStatusUpdationOnArticleIssue(
					articleData.getFolderSeqId(),
					articleData.getArticleSeqid(),
					DRBUtils.setPersonnelFullName(
							ownerUserObj.getUserLastName(),
							ownerUserObj.getUserFirstName()));

			getSimpleJdbcTemplate()
					.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_ISSUE_DATE_QUERY,
							new Object[] {
									userName,
									articleData.getArticleReviewReqId(),
									Integer.parseInt(articleData
											.getArticleSeqid()) });
			articleData.setOldValue("Draft");
			articleData.setNewValue(articleData.getArticleState());
			articleData.setArticleSeqId(Integer.parseInt(articleData
					.getArticleSeqid()));
			articleData.setChangedAttributeName("Issued");
			this.captureArticlesHistory(articleData, ownerUserObj);

			if (articleData.getRevisionArticleSeqId() != null) {
				getSimpleJdbcTemplate()
						.update(DRBQueryConstants.ARTICLE_REVISE_ARTICLE_PROMOTE_ARTICLE_QUERY,
								new Object[] {
										userName,
										articleData.getArticleReviewReqId(),
										Integer.parseInt(articleData
												.getArticleSeqid()) });
				// Need to capture to history
				articleData.setOldValue("Checked-Out");
				articleData.setNewValue("Issued");
				articleData.setArticleSeqId(Integer.parseInt(articleData
						.getRevisionArticleSeqId()));
				articleData.setChangedAttributeName("Issued");
				String userId = ownerUserObj.getUserSSOID();
				getJdbcTemplate()
						.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_STATE_CHANGE_HISTORY_QUERY,
								new Object[] {
										userId,
										articleData.getChangedAttributeName(),
										articleData.getOldValue(),
										articleData.getNewValue(),
										Integer.parseInt(articleData
												.getRevisionArticleSeqId()),
										userName, userName });
			}

		}
		String getUserQuery = DRBQueryConstants.ARTICLE_CREATE_ARTICLE_GETNOTIFICATIONLIST_QUERY;
		RowMapper getGeneralRowMapper = new RowMapper() {
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String email = rs.getString(1);
				return email;
			}
		};
		emailIds = getJdbcTemplate()
				.query(getUserQuery,
						new Object[] { Integer.parseInt(articleData
								.getArticleSeqid()) }, getGeneralRowMapper);

		/**
		 * Commented by Pradeep for Enhancement No: 64
		 */
		try {
			if ((String) ownerUserObj.getUserEMailID()/* .toString() */!= null) {
				emailIds.add((String) ownerUserObj.getUserEMailID()/*
																	 * .toString(
																	 * )
																	 */);// Code
																			// Review
																			// Changed
																			// By
																			// Naresh
																			// on
																			// 22DEC10
			}
		} catch (Exception e) {
			DRBUtils.addErrorMessage(ArticleConstants.DRB_ERROR, null);// Code
																		// Review
																		// Changed
																		// By
																		// Naresh
																		// on
																		// 22DEC10
		}

		viewArticleProc.executeViewArticle(
				Integer.parseInt(articleData.getArticleSeqid()),
				articleData.getArticleOwnerSSO(), "ALL",
				Integer.parseInt(articleData.getFolderSeqId()), articleData,
				"false");
		return articleData;

	}

	private int captureArticlesHistory(ArticleData articleData, UserData userObj) {
		String userId = userObj.getUserSSOID();
		String userName = userObj.getUserLastName() + ", "
				+ userObj.getUserFirstName();
		int historyUpdate = getJdbcTemplate()
				.update(DRBQueryConstants.ARTICLE_CREATE_ARTICLE_UPDATE_STATE_CHANGE_HISTORY_QUERY,
						new Object[] {
								userId,
								articleData.getChangedAttributeName(),
								articleData.getOldValue(),
								articleData.getNewValue(),
								Integer.parseInt(articleData.getArticleSeqid()),
								userName, userName });
		return historyUpdate;
	}

	/**
	 * Update article state to draft when the problem record got created
	 */
	public void updateStateToDraft(String articleSeqId)
			throws DRBCommonException {
		getSimpleJdbcTemplate().update(
				DRBQueryConstants.UPDATE_PROBLEM_RECORD_STATE_TO_DRAFT,
				articleSeqId);
	}

	@Override
	public List<Integer> getRecordActions(List<String> recordActions) throws MappingException {
		int recordActionSeqId = 0;
		List<Integer> recordActionSeqIds = new ArrayList<Integer>();
		try {

			List<String> admin = getSimpleJdbcTemplate().query(
					DRBQueryConstants.FETCH_ALL_RECORD_ACTION, RecordMapper);
			for (int i = 0; i < recordActions.size(); i++) {
				for (int j = 0; j < admin.size(); j++) {
					if (recordActions.get(i).equalsIgnoreCase(admin.get(j))) {
						recordActionSeqId = getSimpleJdbcTemplate()
								.queryForInt(
										DRBQueryConstants.FETCH_RECORD_ACTION_BASED_ON_CODE,
										admin.get(j));
						recordActionSeqIds.add(recordActionSeqId);
					}
				}

			}

		} catch (Exception e) {
			recordActionSeqId = 0;
		}
		
		if(recordActionSeqId == 0 )
		{
			throw new MappingException();
		}	
		return recordActionSeqIds;
	}

	public ArticleData saveRecordAction(ArticleData articleData)
			throws DRBCommonException {

		if ("articleMetaData".equalsIgnoreCase(articleData.getButtonName())) {
			for (int i = 0; i < articleData.getRecordActionCodeId().size(); i++) {
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.SAVE_RECORD_ACTION,
						articleData.getRecordActionCodeId().get(i),
						articleData.getArticleSeqid());
			}
		} else if ("editArticleSave".equals(articleData.getButtonName())) {
			for (int i = 0; i <= articleData.getSelectedEngineSubModels()
					.size(); i++) {
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.DELETE_RECORD_ACTION_MAPPING,
						articleData.getArticleSeqid());
			}
			for (int i = 0; i < articleData.getRecordActionCodeId().size(); i++) {
				getSimpleJdbcTemplate().update(
						DRBQueryConstants.SAVE_RECORD_ACTION,
						articleData.getRecordActionCodeId().get(i),
						articleData.getArticleSeqid());
			}
		}
		return null;
	}

	private ParameterizedRowMapper<String> RecordMapper = new ParameterizedRowMapper<String>() {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {

			String data;
			data = rs.getString("RCRD_ACTN_CODE");
			return data;
		}
	};
}
