app.controller('CreateDBCtrl', function($scope, $state, $http) {
	
	$scope.db = {'port': 3306, 'group': 'dba'}
	$scope.update = function(db) {
		function success() {
			$scope.status = "Ok"
			setTimeout(function() { $state.go("app.main"); }, 2000);
		}
		function error() {
			$scope.status = "Error"
		}
		$scope.status = ""
		$http.post('/masterchef/db', db).then(success, error)
		//setTimeout(function() { success(); $scope.$apply() }, 1000)
	}
})