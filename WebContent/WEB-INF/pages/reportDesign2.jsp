<div ng-controller="reportDesignController">
	<md-content class="md-padding"> <md-card > <md-toolbar style="background-color:/* rgb(0, 121, 128) */rgb(89, 121, 149)"
		class="md-warn">
	<div layout-align="center center" class="md-toolbar-tools">
		<h2 class="md-flex">Report Design</h2>
	</div>
	</md-toolbar> <md-card-content>
	<div flex layout="row">
	<div flex="100">
	<md-card> 
	<md-toolbar style="background-color:rgb(68, 135, 121)" class="custom-ttoolbar">
	<div layout-align="start center" class="md-toolbar-tools">
				<h3   class="md-flex">Report Info</h3>
			</div>
	</md-toolbar>
		<md-content>
		<div layout="row" flex   layout-padding>
		<div  flex="50" >
			 <md-input-container>
			      <label>Report Title</label>
			      <input required ng-model="ReportDesign.reportName">
	    	</md-input-container>
		</div>
		<div  flex="50" >
		<md-checkbox  class="md-warn" ng-model="ReportDesign.visibility" aria-label="visibility">
        visibility : <span ng-if="ReportDesing.visibility===true">Public</span>
         <span ng-if="ReportDesing.visibility!=true">Private</span>
        </md-checkbox>
		</div>
		</div>
		<md-content>
	</md-card>
	
	</div>
	</div>

	<div flex class="fieldsSelection" layout="row">
		<div flex="45">
			<md-card> <md-toolbar class="custom-ttoolbar"  style="background-color:rgb(68, 135, 121);" class="md-warn">
			<div layout-align="center center" class="md-toolbar-tools">
				<h3   class="md-flex"></>Available Fields</h3>
			</div>
			</md-toolbar> <md-content class=" custom-card-content" >
			<div layout="column" class="repeatItemPageLeft"  ng-repeat="item in ReportDesign.leftFields | orderBy" class="left" layout-marging  id="fieldsLeftPane">
			<div flex="100"  class="card-elements" layout="row" layout-align="center center">
				<span ng-class="{'selectdOneText':ReportDesign.selectedLeftFields.indexOf(item)!=-1}" made-selected="fieldsSelection" side="left"><md-button >{{item}}</md-button></span>
			</div>
			
			</div>
			
			</md-content> </md-card>
		</div>
		<div flex="10">
		<div layout="column" layout-margin  layout-align="center center">
			<div flex="row"></div>
			<div flex="row"></div>
			<div flex="100" 	 layout="row">
				<span add-selected="fieldsAdd"  ><md-button aria-label="adding" class="md-fab  report-fab-bg" ><i class="fa fa-2x fa-arrow-right green"></i></i></md-button></span>
			</div>
			<div flex="100"  layout="row">
			<span  remove-selected="fieldsRemove" ><md-button aria-label="removing"  class="md-fab"><i class="fa fa-2x fa-arrow-left " ></i></md-button></span>
			</div>
			
			
		</div>
		
		</div>
		<div flex="45">
			<md-card> 
					<md-toolbar class="custom-ttoolbar" style="background-color:rgb(68, 135, 121)" class="md-warn">
					<div layout-align="center center" class="md-toolbar-tools">
						<h3 class="md-flex">Selected Fields</h3>
					</div>
					</md-toolbar> 
						<md-content class="custom-card-content">
						<div layout="column" class="repeatItemPageRight"  ng-repeat="item in ReportDesign.rightFields | orderBy" layout-marging  id="fieldsRightPane"  >
							<div flex="100"  class="card-elements" layout="row"  class="card-elements" layout-align="center center">
								<span ng-class="{'removedOneText':ReportDesign.selectedRightFields.indexOf(item)!=-1}" made-selected="fieldsSelection" side="right"><md-button >{{item}}</md-button></span>
							</div>
						</div>
						<div layout="row" layout-align="end center">
						<md-button ng-show="ReportDesign.rightFields.length!=0?true:false " class="md-raised md-warn" ng-click="resetSelection('fieldsRefresh')">reset</md-button>
						</div>
						</md-content>
			 </md-card>
		</div>
	</div>
	
	<div flex class="headersSelection" layout="row">
		<div flex="45">
			<md-card> <md-toolbar class="custom-ttoolbar"  style="background-color:rgb(68, 135, 121);" class="md-warn">
			<div layout-align="center center" class="md-toolbar-tools">
				<h3   class="md-flex"></>Available Headers</h3>
			</div>
			</md-toolbar> <md-content class="custom-card-content">
			<div layout="column"   class="repeatItemPageLeft" ng-repeat="item in ReportDesign.leftHeaders | orderBy " layout-marging  id="headersLeftPane">
			<div flex="100" layout="row" class="card-elements"  layout-align="center center">
				<span ng-class="{'selectdOneText':ReportDesign.selectedLeftHeaders.indexOf(item)!=-1}" made-selected="headersSelection" side="left"><md-button >{{item}}</md-button></span>
			</div>
			
			</div>
			
			
			
			</md-content> </md-card>
		</div>
		<div flex="10">
		<div layout="column" layout-margin layout-padding layout-align="center center">
			<div flex="row"></div>
			<div flex="row"></div>
			<div flex="100" 	 layout="row">
				<span add-selected="headersAdd" ><md-button aria-label="hadding" class="md-fab  report-fab-bg" ><i class="fa fa-2x fa-arrow-right green"></i></i></md-button></span>
			</div>
			<div flex="100"  layout="row">
			<span remove-selected="headersRemove" ><md-button aria-label="hremoving"  class="md-fab"><i class="fa fa-2x fa-arrow-left " ></i></md-button></span>
			</div>
			
			
		</div>
		
		</div>
		<div flex="45">
			<md-card> 
					<md-toolbar class="custom-ttoolbar" style="background-color:rgb(68, 135, 121)" class="md-warn">
					<div layout-align="center center" class="md-toolbar-tools">
						<h3 class="md-flex">Selected Headers</h3>
					</div>
					</md-toolbar> 
						<md-content class="custom-card-content">
						<div layout="column" class="repeatItemPageRight"  ng-repeat="item in ReportDesign.rightHeaders| orderBy" layout-marging  id="headersRightPane"   >
							<div flex="100"  class="card-elements" layout="row" layout-align="center center">
								<span ng-class="{'removedOneText':ReportDesign.selectedRightHeaders.indexOf(item)!=-1}" made-selected="headersSelection" side="right"><md-button >{{item}}</md-button></span>
							</div>
						</div>
						<div layout="row" layout-align="end center">
						<md-button ng-show="ReportDesign.rightHeaders.length!=0?true:false" class="md-raised md-mini md-warn"  ng-click="resetSelection('headersRefresh')">reset</md-button>
						</div>
						
						</md-content>
			 </md-card>
		</div>
	</div>

	<div layout="row" layout-align="center" flex>
		<div layout="column" layout-padding flex>
			<div layout="row" layout-align="center" flex>
			<md-button ng-show="allSelections" class="md-raised md-warn" ng-click="resetSelection('ALL')" class="md-raised md-primary">Reset</md-button>
			</div>
			<div layout="row" layout-align="center" flex>
				<md-button  ng-click="saveFieldsDetails()" class="md-raised md-primary">Generate Report </md-button>
			</div>
		</div>

	</div>



	</md-card-content>
	</md-card> 
    </md-content>
</div>