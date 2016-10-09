app.controller('MainCtrl', function($scope, $state,  $http, $window, user) {
	
	var resetMessages = function() {
		$scope['created'] = false;
		$scope['finished'] = false;
	}	
	
	$scope.generateUser = function() {
		resetMessages();
		$http.get('user/' + $scope.jiraID).then(function(results) {
			$scope.dbuser = results.data;
			$scope['created'] = true;
		});
	}
	
	$scope.finishIssue = function() {
		resetMessages();
		$http.delete('user/' + $scope.jiraID).then(function(results) {
			$scope['finished'] = results.data;
		})
	}

    var loadDatabases = function() {
        $http.get("/masterchef/db").then(function(results) {
            $scope.databases = results.data;
        });
    }

	user.then(function(response)  {
		var groups = response.data.groups;
		var dba = false;
		for (var i = 0, len = groups.length; i < len; i++) {
			if (groups[i].name == "dba") {
				dba = true;
                loadDatabases();
				break;
			}
		}
		$scope.dba = dba;	
	})

	$scope.deleteDB = function(db) {
		function success() {
			$scope.status = "Ok"
			setTimeout(loadDatabases, 2000);
		}
		function error() {
			$scope.status = "Error"
		}
		if(confirm("¿Está seguro que quiere borrar la base de datos " + db.host + "/" + db.name + "?")) {
            $scope.status = ""
            $http.delete('/masterchef/db?host='+db.host+'&name='+db.name).then(success, error)
		}
	}

})