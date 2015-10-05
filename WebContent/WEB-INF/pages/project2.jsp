<html ng-app="ReportDesign2">
<title>PAAM-ADMIN</title>
<head>

<script	src="<%=request.getContextPath()%>/resources/javascript/angular.js"></script>

<script	src="<%=request.getContextPath()%>/resources/javascript/angular-animate.js"></script>

<script	src="<%=request.getContextPath()%>/resources/javascript/angular-aria.js"></script>

<script	src="<%=request.getContextPath()%>/resources/javascript/angular-route.js"></script>

<!-- Angular Material Javascript now available via Google CDN; version 0.8 used here -->

<script	src="<%=request.getContextPath()%>/resources/javascript/angular-material.js"></script>


<script	src="<%=request.getContextPath()%>/resources/javascript/jquery-2.1.3.min.js"></script>

<script src="<%=request.getContextPath()%>/resources/javascript/app2.js"></script>


<link rel="stylesheet"	href="<%=request.getContextPath()%>/resources/css/font-awesome.min.css">

<link rel="stylesheet"	href="<%=request.getContextPath()%>/resources/javascript/angular-material.css">




</head>

<style>
.custom-card-content{
max-height: 200px;
 /* overflow-x:auto;  */
}
.card-elements{
padding-bottom: 7px;

}

.selectdOneText{
background: rgb(106, 226, 106);
border-radius:2px;
-webkit-border-radius:5px;
font-size: 20px;
}
.custom-ttoolbar{
height: 40px;
min-height: 40px;
}
.removedOneText{
background: rgb(252, 94, 94);
border-radius:2px;
-webkit-border-radius:5px;
font-size: 20px;

}


.md-button.md-default-theme.md-fab{
background-color: rgb(89, 121, 149)
}

a {
	text-decoration: none !important;
}

.toolbardemoBasicUsage md-toolbar md-icon.md-default-theme {
	color: white;
}

.header {
	background-color: red !important
}

.heading {
	padding-left: 25px;
}

md-tabs.md-default-theme md-tabs-ink-bar {
	color: rgb(19, 232, 19) !important;
}

/* ------------------------------------------------- */

.repeatItemPageLeft.ng-enter  {
-webkit-animation: 1s ng-enter-repeat-animation;
animation: 1s ng-enter-repeat-animation;
}
@-webkit-keyframes ng-enter-repeat-animation {
from {

opacity: 1; 
        -webkit-transform: translateX(2000px); 
}
to {

opacity: 1; 
        -webkit-transform: translateX(0px); 
}
}
@keyframes ng-enter-repeat-animation {
from {

opacity: 1; 
        -webkit-transform: translateX(2000px); 
}
to {

opacity: 1; 
        -webkit-transform: translateX(0px); 
}
}



.repeatItemPageLeft.ng-leave {
-webkit-animation: 2s ng-leave-repeat-animation;
animation: 2s ng-leave-repeat-animation;
}
@-webkit-keyframes ng-leave-repeat-animation {
from {

opacity: 1; 
        -webkit-transform: translateX(0); 
}
to {
position:absolute;
opacity: 1; 
        -webkit-transform: translateX(2000px); 
}
}
@keyframes ng-leave-repeat-animation {
from {
opacity: 1; 
        -webkit-transform: translateX(0); 
}
to {
opacity: 1; 
        -webkit-transform: translateX(2000px); 
color: red;
}
}

/* ----------- */
.repeatItemPageRight.ng-leave{
-webkit-animation: 1s ng-leave-repeat-animation2;
animation: 1s ng-leave-repeat-animation2;
}
@-webkit-keyframes ng-leave-repeat-animation2 {
from {
opacity: 1; 
        -webkit-transform: translateX(0); 
}
to {
opacity: 1; 
        -webkit-transform: translateX(-2000px); 
}
}
@keyframes ng-leave-repeat-animation2 {
from {
opacity: 1; 
        -webkit-transform: translateX(0); 
}
to {
opacity: 1; 
        -webkit-transform: translateX(-2000px); 
}
}



.repeatItemPageRight.ng-enter{
-webkit-animation: 2s ng-enter-repeat-animation2;
animation: 2s ng-enter-repeat-animation2;
}
@-webkit-keyframes ng-enter-repeat-animation2 {
from {
opacity: 1; 
        -webkit-transform: translateX(-2000px); 
}
to {
opacity: 1; 
        -webkit-transform: translateX(0); 
}
}
@keyframes ng-enter-repeat-animation2 {
from {
opacity: 1; 
        -webkit-transform: translateX(-2000px); 
}
to {
opacity: 1; 
        -webkit-transform: translateX(0); 
color: red;
}
}
</style>

<body>


	<div>
		<md-content > 
		<md-toolbar class="header">
		<div class="md-toolbar-tools">
			<span><img src="<%=request.getContextPath()%>/resources/images/capitallogo.png" height="50px"
				width="75px"></span>
			<div style="text-align: center">
				<h2 class="heading">Cisco - Capital</h2>
			</div>
			
		</div>
		</md-toolbar>
		 <md-tabs md-stretch-tabs="always"> <a href="#/home"> <md-tab>Home</md-tab></a>
		<a href="#/login"> <md-tab>Upload</md-tab></a> <a href="#/reportDesign2"><md-tab>Report Design</md-tab></a>
		</md-tabs> <br>
	<div ng-view  layout-align-center></div>
		</md-content>
	
		</div>


	<!-- Angular Material Dependencies -->
	

	
</body>

</html>
