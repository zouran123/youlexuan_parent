app.controller('contentController', function ($scope, contentService) {

    $scope.findByCategoryId = function (catId) {
        contentService.findByCategoryId(catId).success(function (resp) {
            if(resp) {
                $scope.contentList = resp;
            }
        });
    }

    $scope.toSearch = function () {
        location.href = "http://localhost:9007/search.html#?kw=" + $scope.keywords;
    }


});
