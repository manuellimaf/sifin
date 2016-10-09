app.factory('reponseHandler',  function($q, $injector, $window ) {  
    var requestRecoverer = {
      	 'responseError': function(rejection ) {
    		var service = $injector.get("ngDialog");
    		var data = rejection.data;
    		console.log("Rejection $http fail");
    		console.log(rejection);
    		var dialog;
    		if(rejection.status == "450" || rejection.status == "503" ) {
    			// User exception
	    		dialog = { 
	    			'template': 'userExTemplate' , 
	        		'overlay': true,
	        		'className': 'ngdialog-theme-default ngdialog-theme-user-exception',
	        		'controller':  function($scope) {
	        			$scope.status = rejection.status;
	        			$scope.errorType = data.error_type;
	        			$scope.errorMessage = data.error_message;
	       		    }
	   			}
	    		service.open(dialog);
    		} else if(rejection.status == "440") {
    			// Session expired - redirect
    			$window.location.href = data;
    		} else {
    			// Unhandled error
	    		dialog = { 
	    			'template': 'templateId' , 
	        		'overlay': true,
	        		'className': 'ngdialog-theme-default ngdialog-theme-exception',
	        		'controller':  function($scope) {
	        			$scope.status = rejection.status;
	        			$scope.request = data.request;
	        			$scope.errorType = data.error_type;
	        			$scope.errorMessage = data.error_message;
	        			//$scope.stackTrace = data.stackTrace.join("<br>");
	       		    }
	   			}
	    		service.open(dialog);
    		}
    	    return $q.reject(rejection);
      	 }
    }
    return requestRecoverer;
});