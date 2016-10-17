app.controller('MonthlyCtrl', function($scope, $state,  $http, $window, months, movements) {
	$scope.months = months.data;
	$scope.movements = movements.data;

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