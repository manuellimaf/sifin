var requires = [ 'angular-loading-bar', 'ui.router', 'ngDialog', 'ngCookies', 'highcharts-ng' ];

app = angular.module('sifin', requires);

app.config(function($stateProvider, $urlRouterProvider, $locationProvider, $httpProvider, $urlMatcherFactoryProvider) {

	$httpProvider.interceptors.push('reponseHandler');

	$stateProvider

	.state('app', {
		abstract : true,
		url : '/',
        views : {
			'links' : {
				templateUrl : 'templates/links.html'
			},
			'' : {
				template : '<ui-view></ui-view>',
			}
        }
	})

	.state('app.monthly', {
		url : 'monthly',
		templateUrl : 'templates/monthly.html',
		resolve : {
			months: function($http) {
				return $http({
					method : 'GET',
					url : 'months'
				});
			}
		},
		controller : 'MonthlyCtrl'
	})

	.state('app.anual', {
    		url : 'anual',
    		templateUrl : 'templates/anual.html',
    		controller : 'AnualCtrl'
    	});

	$urlRouterProvider.otherwise("monthly");
});