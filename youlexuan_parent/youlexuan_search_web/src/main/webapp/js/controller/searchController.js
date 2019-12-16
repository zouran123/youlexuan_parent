app.controller('searchController', function($scope, $location, searchService){

    $scope.search = function () {
        searchService.search($scope.searchMap).success(function(resp){
                if(resp) {
                    $scope.resultMap = resp;
                    // 开始造页码
                    createPageNum();
                }
            }
        );
    }

    $scope.toSearch = function() {
        $scope.searchMap.category = '';
        $scope.searchMap.brand = '';
        $scope.searchMap.spec = {};
        $scope.searchMap.pageNo = 1;
        $scope.searchMap.price = '';
        $scope.searchMap.sortName = '';
        $scope.searchMap.sortVal = '';

        $scope.search();
    }

    createPageNum = function() {
        $scope.pageShow = [];
        var firstPage = 1;
        var lastPage = $scope.resultMap.totalPages;

        $scope.firstDian = true;
        $scope.lastDian = true;

        if($scope.resultMap.totalPages > 5) {
            if($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDian = false;
            } else if($scope.searchMap.pageNo >= $scope.resultMap.totalPages - 2) {
                firstPage = $scope.resultMap.totalPages - 4;
                $scope.lastDian = false;
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDian = false;
            $scope.lastDian = false;
        }

        for (var i = firstPage; i <= lastPage ; i++) {
            $scope.pageShow.push(i);
        }
    }


    //搜索对象
    $scope.searchMap = {'keywords':'', 'category':'', 'brand':'', 'spec':{}, 'price':'', 'pageNo':1,'pageSize':20, 'sortName':'','sortVal':''};

    $scope.addSearch = function (key, value) {
        if(key == 'category' || key  == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }

        $scope.search();
    }

    $scope.removeSearch = function(key) {
        if(key == 'category' || key  == 'brand' || key == 'price') {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];
        }

        $scope.search();
    }

    // 跳转页面
    $scope.jumpPage = function (pageNo) {

        pageNo = parseInt(pageNo);

        if(pageNo < 1 || pageNo > $scope.resultMap.totalPages ) {
            return;
        }

        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }


    // 排序
    $scope.addSort = function (sortField, sortVal) {
        $scope.searchMap.sortName = sortField;
        $scope.searchMap.sortVal = sortVal;
        $scope.search();
    }

    // 进入页面加载关键字
    $scope.initKw = function() {
        var kw = $location.search()['kw'];
        $scope.searchMap.keywords = kw;
        $scope.search();
    }

});
