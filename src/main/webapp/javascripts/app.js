var requires = [ 'angular-loading-bar', 'ui.router', 'ngDialog', 'ngCookies' ]

app = angular.module('masterchef', requires)

app.factory("user", function($http) {
	var promise = $http.get("/masterchef/user");
	return promise
})

app.config(function($stateProvider, $urlRouterProvider, $locationProvider,
		$httpProvider, $urlMatcherFactoryProvider) {

	$httpProvider.interceptors.push('reponseHandler');

	$stateProvider

	.state('app', {
		abstract : true,
		url : '/',
		views : {
			'links' : {
				templateUrl : 'templates/links.html',
				controller : function($scope, links, user) {
					$scope.links = links.data
					user.then(function(response) {
						$scope.username = response.data.name
					})
					$scope.open = false
				},
				resolve : {
					"links" : function($http) {
						return $http.get("/masterchef/links")
					}
				}
			},
			'' : {
				template : '<ui-view></ui-view>',
			}
		}
	})

	.state('app.main', {
		url : "main",
		controller : 'MainCtrl',
		templateUrl : 'templates/main.html'
	})

	.state('app.create-db', {
		url : "create-db",
		controller : 'CreateDBCtrl',
		templateUrl : 'templates/create-db.html'
	})

	$urlRouterProvider.otherwise("/main")
})