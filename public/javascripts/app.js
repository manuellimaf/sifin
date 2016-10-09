var requireds = [];
var app = angular.module('sifinApp', requireds);

//app.constant('sifin', '/sifin');
//app.constant('sifinApi', '/sifinapi');
app.constant('sifin', '/');

app.config(function($stateProvider) {
	var templates = sifin + '/assets/templates';

	$stateProvider
		.state('home', {
			url: sifin,
			controller: function($state) { $state.go('month') }
		})
		
		.state('month', {
			url: sifin + "/month",
			templateUrl: templates + '/month.html',
			controller: 'MonthView'
		});
});