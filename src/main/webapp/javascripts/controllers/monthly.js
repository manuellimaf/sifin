app.controller('MonthlyCtrl', function($scope, $state,  $http, $window, months) {
	$scope.months = months.data;
	$scope.movements = {
	    "income": {"own": 10000.5, "other": 10.33},
	    "expenses": {"cash": 2000.4, "tc": 200.0, "taxes": 1000.4, "total": 3200.8, "estimated": 1500},
	    "savings": 3000,
	    "invested": 1000,
	    "available": 2800,
	    "usd-price": 15.44
	};

    $scope.categoryChart = {
        options: {
            chart: {
                type: 'bar'
            },
            legend: { enabled: false }
        },
        series: [{
            data: [
                ["Supermercado", 100],
                ["Comida trabajo", 150],
                ["Comida fuera", 120],
                ["Salidas", 89],
                ["Transporte", 170],
                ["Educación", 0],
                ["Salud", 10],
                ["Extras", 70]
            ],
            dataLabels: {enabled: true}
        }],
        title: {
            text: 'Gasto por categoría'
        },
        yAxis: { title: { enabled: false } },
        loading: false
    };
    $scope.dailyChart = {
        options: {
            chart: {
                type: 'column'
            },
             legend: { enabled: false }
        },
        series: [{
            data: [100, 150, 120, 89, 70, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        }],
        title: {
            text: 'Gasto por día'
        },
        yAxis: { title: {enabled: false } },
        loading: false
    };

});