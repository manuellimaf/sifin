app.controller('MonthlyCtrl', function($scope, $state,  $http, $window, $stateParams, months, movements, categories, expensesByCat, expensesByDay) {
	$scope.months = months.data;
	$scope.movements = movements.data;
    $scope.month = { id: $stateParams['monthId'] };

    var onMonthChange = function() {
        $state.go("app.monthly", { 'monthId': $scope.month.id });
    };

    $scope.$watch('month', onMonthChange);

    $scope.categoryChart = {
        options: {
            chart: {
                type: 'bar'
            },
            legend: { enabled: false }
        },
        title: {
            text: 'Gasto por categoría'
        },
        yAxis: {
            title: { enabled: false },
             },
        xAxis: { categories: categories.data },
        series: expensesByCat.data,
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
            data: expensesByDay.data
        }],
        title: {
            text: 'Gasto por día'
        },
        yAxis: { title: {enabled: false } },
        xAxis: {
            allowDecimals: false,
            tickInterval: 1
        },
        loading: false
    };

});