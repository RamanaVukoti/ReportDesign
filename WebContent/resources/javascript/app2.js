
     var app =  angular.module('ReportDesign2', ['ngMaterial','ngRoute','ngAria','ngAnimate']);
     
     app.config(function($routeProvider) {
			$routeProvider.when("/home", {
				templateUrl : "home.htm"
			}).when("/login", {
				templateUrl : "login.htm"
			}).when("/registration", {
				templateUrl : "registration"
			}).when("/reportDesign2", {
				templateUrl : "reportDesign2.htm"
			}).otherwise({
				redirectTo : "/home"
			});
		});
  
     
     app.controller('reportDesignController',ReportDesignController);
     app.controller('dialogController',DialogController);
     app.directive('madeSelected',madeSelected);
     app.directive('addSelected',AddSelected);
     app.directive('removeSelected',RemoveSelected);
     
     
     
     
  //addindg selected elm to array   
function madeSelected(){
    	 
    	 return{
    		 rstrict:'A',
    		 link:function(scope,element,attr){
    			 element.on('click',function(event){
    				 event.preventDefault();
    				 
    				 var elmTxt=element.text();
    				 if(attr.madeSelected=="fieldsSelection" ){
    					 
    					 if(attr.side=="left"){
    						 madeSelectedHelper(elmTxt,scope.ReportDesign.selectedLeftFields);
    					 }
    					 else if(attr.side="right"){
    						 madeSelectedHelper(elmTxt,scope.ReportDesign.selectedRightFields);
    					 }
    	    				
    				 }else if(attr.madeSelected=="headersSelection"){
    					 if(attr.side=="left"){
    						 madeSelectedHelper(elmTxt,scope.ReportDesign.selectedLeftHeaders);
    					 }
    					 else if(attr.side=="right"){
    						 madeSelectedHelper(elmTxt,scope.ReportDesign.selectedRightHeaders);
    					 }
    				 }
    				 //css for selected elements
    				/* if($(element).parent().parent().attr('id').indexOf('LeftPane')>-1){
    					 $(element).toggleClass('selectdOneText');
    					 $(element).parent().toggleClass('selectedOneDiv');
    					 
    				 }
    				 if($(element).parent().parent().attr('id').indexOf('RightPane')>-1){
    					 $(element).toggleClass('removedOneText');
    					 $(element).parent().toggleClass('removedOneDiv');
    					 
    				 }*/
    				
    				 scope.$apply();
    				 
    			 });
    		 }
    		 
    	 };
     }
//pushing selected elments to respective arrays
	function madeSelectedHelper(elTxt, tArray){
		if($.inArray(elTxt,tArray)>-1){
				var index=tArray.indexOf(elTxt);
				tArray.splice(index,1);
			}else{
				tArray.push(elTxt);
			}
	}
	
	//LEFT to RIGHT
     function AddSelected($compile){
    	 return {
    		 restrict:'A',
    		 link:function(scope,element,attrs){
    			 element.on('click',function(){
    				 if(attrs.addSelected=='fieldsAdd'){
    					 addingRemovingHelper( scope.ReportDesign.rightFields,scope.ReportDesign.selectedLeftFields,scope.ReportDesign.leftFields);
    					 scope.ReportDesign.selectedLeftFields=[];
    				 }else if(attrs.addSelected=='headersAdd'){
    					 addingRemovingHelper( scope.ReportDesign.rightHeaders,scope.ReportDesign.selectedLeftHeaders,scope.ReportDesign.leftHeaders);
    					 scope.ReportDesign.selectedLeftHeaders=[];
    				 }
    				scope.$apply();
    				
    				
    			 });
    		 }	
    		 
    	 };
    	 
     }
     
     //RIGHT to Left
     function RemoveSelected (){
    	 return {
    		 restrict:'A',
    		 link:function(scope,element,attr){
    			 element.on('click',function(){
    				 
    				 if(attr.removeSelected=='fieldsRemove'){
    					 addingRemovingHelper( scope.ReportDesign.leftFields,scope.ReportDesign.selectedRightFields,scope.ReportDesign.rightFields);
    					 scope.ReportDesign.selectedRightFields=[];
        				
    				 }else if(attr.removeSelected=='headersRemove'){
    					 addingRemovingHelper( scope.ReportDesign.leftHeaders,scope.ReportDesign.selectedRightHeaders,scope.ReportDesign.rightHeaders);
    					 scope.ReportDesign.selectedRightHeaders=[];
    				 }
    				scope.$apply();
     				
    			 });
    		 }
    	 };
     }
     
     //adding selected elm to the target array , removing elm from base Arry  and resetting sourec (selectdelms) array
     function addingRemovingHelper(targetArry, sourceArry, baseArry) {
    	 for (var i = 0; i < sourceArry.length; i++) {
				baseArry.splice(baseArry.indexOf(sourceArry[i]), 1);
			}
			targetArry.push.apply(targetArry, sourceArry);
			
		
     }
    
     
     function ReportDesignController($scope,$http,$mdDialog,$rootScope){
    	 $scope.ReportDesign={};
    	 $scope.allSelections=true;
    	 
    	 $scope.ReportDesign.leftFields=['field0','field1','field2','field3','field4','field5','field6','field7','field8','field9','field10','field11','field12'];
    	 $scope.ReportDesign.rightFields=[];
    	 
    	 $scope.ReportDesign.leftHeaders=['header0','header1','header2','header3','header4','header5','header6','header7','header8','header9','header10','header11','header12'];
    	 $scope.ReportDesign.rightHeaders=[];
    	 
    	 $scope.ReportDesign.selectedLeftFields=[];
    	 $scope.ReportDesign.selectedRightFields=[];
    	 
    	 $scope.ReportDesign.selectedLeftHeaders=[];
    	 $scope.ReportDesign.selectedRightHeaders=[];
    	 $rootScope.$on("dialogOk",function(){
    		 $http({
					method : "GET",
					url    : "saveFieldDetails.htm",
					params : $scope.ReportDesign
				}).success(function(data, status, headers, config,event){
					
				}).error(function(data, status, headers, config){
				});
				console.log($scope.ReportDesing);

				$mdDialog.hide();
			});
    	 
    	 $rootScope.$on("dialogCancel",function(){
				$mdDialog.hide();
			});
    	 
    	 
		$scope.saveFieldsDetails = function(event) {
			$scope.ReportDesign.finalHeaders='';
			$scope.ReportDesign.finalFields='';
			$scope.ReportDesign.finalHeaders=splitArry($scope.ReportDesign.rightHeaders);
			$scope.ReportDesign.finalFields=splitArry($scope.ReportDesign.rightFields);
			
    	 
				var confirm= $mdDialog.alert({
		    		parent:angular.element(document.body),
		    		targetEvent: event,
		    		ariaLablel:'alerts',
		    		template:"<md-dialog aria-label='report info'>" +
		    		"<md-toolbar class='custom-ttoolbar'>" +
					
					"<div layout-align='center center' class='md-toolbar-tools'>"+
					"<h2 class='md-flex'>Report Design - Review </h2>"+
					"</div>"+
					"</md-toolbar>" +
		    				"<md-content>" +
		    				"<div layout='column' flex='100' layout-padding layout-wrap >"+
		    				"<div flex layout='row' ><b>Report Name :</b>"+$scope.ReportDesign.reportName+"</div>" +
		    				"<div flex layout='row' ><b>Selected Fields : </b>"+$scope.ReportDesign.finalFields+"</div>" +
		    				"<div flex layout='row' ><b>Selected Headers : </b>"+$scope.ReportDesign.finalHeaders+"</div>" +
		    				"</div>" +
		    				"<div class='md-actions'>" +
		    				"<md-button class='md-raised md-warn' ng-click='dialogCancel()'>" +
		    				"Cancel" +
		    				"</md-button>" +
		    				"<md-button style='background-color:rgb(53, 189, 53);color :black' class='md-raised' ng-click='dialogOk()'>" +
		    				"Save" +
		    				"</md-button>" +
		    				"</div>" +
		    				"</md-content>" +
		    				"</md-dialog>",
		    				controller:'dialogController'
		    	 });
				
				
				if($scope.ReportDesign.rightFields.length==0 && $scope.ReportDesign.rightHeaders.length==0 ){
					showAlert('At least one Field  and Header is  Required',$mdDialog);
				}else if($scope.ReportDesign.reportName==undefined){
					showAlert('ReportName is mandatory',$mdDialog);
				}else if($scope.ReportDesign.rightHeaders.length==0){
					showAlert('At least one Header is Required',$mdDialog);
				}else if($scope.ReportDesign.rightFields.length==0){
					showAlert('At least one Field  is Required',$mdDialog);
				}
				else{
					$mdDialog.show(confirm);
				}
			};
			$scope.resetSelection=function(arg){
				if(arg=="fieldsRefresh"){
					$scope.ReportDesign.leftFields.push.apply($scope.ReportDesign.leftFields,$scope.ReportDesign.rightFields);
					$scope.ReportDesign.rightFields=[];	
				}else if(arg=="headersRefresh"){
					$scope.headerRefresh=false;
					$scope.ReportDesign.leftHeaders.push.apply($scope.ReportDesign.leftHeaders,$scope.ReportDesign.rightHeaders);
					$scope.ReportDesign.rightHeaders=[];
					
				}
				else if(arg=='ALL'){
					$scope.ReportDesign.leftFields.push.apply($scope.ReportDesign.leftFields,$scope.ReportDesign.rightFields);
					$scope.ReportDesign.rightFields=[];	
					$scope.ReportDesign.leftHeaders.push.apply($scope.ReportDesign.leftHeaders,$scope.ReportDesign.rightHeaders);
					$scope.ReportDesign.rightHeaders=[];
					$scope.ReportDesign.reportName=null;
					$scope.ReportDesign.visibility=null;
				
				}
			};		
			
		}
     
     
	 function splitArry(source){
		 var value="";
		 
		 for(var i=0;i<source.sort().length;i++){
			 value+=source[i]+",";
		 }
		 return value;
	 }
     function showAlert(msg,dialog){
    	 dialog.show(
    			 dialog.alert({
			    	  parent:angular.element(document.body),
			    	  targetEvent:event,
			    	  ariaLabel:'error',
			    	  template:'<md-dialog><div flex layout="row" layout-margin layout-padding >'+msg+'</div><div  class="md-actions"><md-button ng-click="alertOk()">Ok</md-button></div></md-dialog.',
			    	  controller:'dialogController'
			      })
			    );
    	 
     }
     //forming commun. bn controllers(on click on dilaog ctrl triggering ajax event in main ctrler)
     function DialogController($scope,$mdDialog){
    	 $scope.alertOk=function(){
    		 $mdDialog.hide();
    	 };
    	 $scope.dialogOk=function(){
    		 $scope.$emit('dialogOk');
    		 $mdDialog.hide();
    	 };
    	 $scope.dialogCancel=function(){
    		 $scope.$emit('dialogCancel');
    		 $mdDialog.hide();
    	 };
     }
     
		
