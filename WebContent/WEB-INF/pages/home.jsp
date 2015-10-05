
<script>
angular.module('toolbarDemo1').controller("HomeController",function($scope){
    
	$scope.startrAnim=function(){
		$scope.nameClass="anim"
	},
	$scope.stopaAnim=function(){
		$scope.nameClass=""
	}
});


</script>

<style>
/* .anim-add,.anim-remove{

 -webkit-transition:all cubic-bezier(0.250, 0.460, 0.450, 0.940) 3s;
  -moz-transition:all cubic-bezier(0.250, 0.460, 0.450, 0.940) 3s;
  -o-transition:all cubic-bezier(0.250, 0.460, 0.450, 0.940) 3s;
  transition:all cubic-bezier(0.250, 0.460, 0.450, 0.940) 3s;
} */
.anim,.anim-add,.anim-add-active{
-webkit-animation-duration: 2s; 
    animation-duration: 2s; 
    -webkit-animation-fill-mode: both; 
    animation-fill-mode: both;
    font-size: 50px;
    transition :font-size 2s
    
    
}
.anim-remove,.anim-remove-active{
-webkit-animation-duration: 2s; 
    animation-duration: 2s; 
    -webkit-animation-fill-mode: both; 
    animation-fill-mode: both;
    
}

.anim-add { 
    -webkit-animation-name: bounceInRight; 
    animation-name: bounceInRight; 
    
}
.anim-remove{
  -webkit-animation-name: bounceOutUp; 
    animation-name: bounceOutUp; 
}
@keyframes bounceOutUp { 
    0% { 
        transform: translateY(0); 
    } 
    20% { 
        opacity: 1; 
        transform: translateY(20px); 
    } 
    100% { 
        opacity: 0; 
        transform: translateY(-2000px); 
    } 
} 
@keyframes bounceInRight { 
    0% { 
        opacity: 0; 
        transform: translateX(200px); 
    } 
    60% { 
        opacity: 1; 
        transform: translateX(-30px); 
    } 
    80% { 
        transform: translateX(10px); 
    } 
    100% { 
        transform: translateX(0); 
    } 
} 
.view-animate.ng-enter{
  /* -webkit-animation-name: bounceInRight; 
    animation-name: bounceInRight;  */
   /*   -webkit-animation-name: rotateInUpLeft; 
    animation-name: rotateInUpLeft; 
    */
      -webkit-animation-name: flipInX; 
    animation-name: flipInX; 
   
}
.view-animate.ng-leave{
/* -webkit-animation-name: bounceOutUp; 
    animation-name: bounceOutUp;  */
    /*   -webkit-animation-name: rotateInDownRight; 
    animation-name: rotateInDownRight;  */
      -webkit-animation-name: flipOutX; 
    animation-name: flipOutX; 
}


/* .ng-enter,.ng-enter-active{
-webkit-animation-duration: 3s; 
    animation-duration: 3s; 
    -webkit-animation-fill-mode: both; 
    animation-fill-mode: both; */
  /* animation-delay:3s  */
}
/* .ng-leave,.ng-leave-active{
-webkit-animation-duration: 2.5s; 
    animation-duration: 2.5s; 
    -webkit-animation-fill-mode: both; 
    animation-fill-mode: both; */
    
}
@keyframes flipOutX { 
    0% { 
        transform: perspective(400px) rotateX(0deg); 
        opacity: 1; 
    } 
    100% { 
        transform: perspective(400px) rotateX(90deg); 
        opacity: 0; 
    } 
} 
@keyframes flipInX { 
    0% { 
        transform: perspective(400px) rotateX(90deg); 
        opacity: 0; 
    } 
    40% { 
        transform: perspective(400px) rotateX(-10deg); 
    } 
    70% { 
        transform: perspective(400px) rotateX(10deg); 
    } 
    100% { 
        transform: perspective(400px) rotateX(0deg); 
        opacity: 1; 
    } 
} 
.flipInX { 
    -webkit-backface-visibility: visible !important; 
    -webkit-animation-name: flipInX; 
    backface-visibility: visible !important; 
    animation-name: flipInX; 
}
@keyframes rotateInUpLeft { 
    0% { 
        transform-origin: left bottom; 
        transform: rotate(90deg); 
        opacity: 0; 
    } 
    100% { 
        transform-origin: left bottom; 
        transform: rotate(0); 
        opacity: 1; 
    } 
} 
.rotateInUpLeft { 
   
}
@keyframes rotateInDownRight { 
    0% { 
        transform-origin: right bottom; 
        transform: rotate(90deg); 
        opacity: 0; 
    } 
    100% { 
        transform-origin: right bottom; 
        transform: rotate(0); 
        opacity: 1; 
    } 
} 
@keyframes rotateOutUpRight { 
    0% { 
        transform-origin: right bottom; 
        transform: rotate(0); 
        opacity: 1; 
    } 
    100% { 
        transform-origin: right bottom; 
        transform: rotate(90deg); 
        opacity: 0; 
    } 
} 
.rotateOutUpRight { 
  

@keyframes bounceInRight { 
    0% { 
        opacity: 0; 
        transform: translateX(2000px); 
    } 
    60% { 
        opacity: 1; 
        transform: translateX(-30px); 
    } 
    80% { 
        transform: translateX(10px); 
    } 
    100% { 
        transform: translateX(0); 
    } 
} 
@keyframes bounceInLeft { 
    0% { 
        opacity: 0; 
        transform: translateX(-2000px); 
    } 
    60% { 
        opacity: 1; 
        transform: translateX(30px); 
    } 
    80% { 
        transform: translateX(-10px); 
    } 
    100% { 
        transform: translateX(0); 
    } 
} 
</style>


<div ng-controller="HomeController">

<md-content class="md-padding">
<div layout="row" >
<div flex hide-sm></div>
<div flex >
    <md-card>
      <md-card-content>
        <h2 class="md-title">Home</h2>
        <p>
          The titles of Washed Out's breakthrough song and the first single from Paracosm share the
          two most important words in Ernest Greene's musical language: feel it. It's a simple request, as well...<br>
          <div layout="row" layout-align="end end">
          	<md-input-container>
          <md-select placeholder="State" ng-model="ctrl.userState">
        <md-option  value="Ram">Ram</md-option>
        <md-option  value="Ram2">Ram2</md-option>
        <md-option  value="Ram3">Ram3</md-option>
        <md-option  value="Ram4">Ram4</md-option>
        
      </md-select> 	
        <input type="text"/>
      </md-input-container>
       
         
           <md-button class="md-raised md-primary" ng-click="startrAnim()"  >animate</md-button>
<md-button class="md-raised md-warn" ng-click="stopaAnim()">Reset</md-button>
</div>
<md-content ng-class="nameClass" ng-mouseover="startrAnim()" ng-mouseout="stopAnim()" >Test Data</md-content>
        </p>
      </md-card-content>
      <div class="md-actions" layout="row" layout-align="end center">
        <md-button>Action 1</md-button>
        <md-button>Action 2</md-button>
      </div>
    </md-card>
    </div>
    <div flex hide-sm></div>
    </md-content>

</div>
<script>

</script>