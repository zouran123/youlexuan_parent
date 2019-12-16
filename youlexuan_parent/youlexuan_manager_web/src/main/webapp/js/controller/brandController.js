app.controller('brandController', function($scope, $controller, brandService){
    // 继承
    $controller('baseController', {
        $scope: $scope
    });

    $scope.findAll = function() {
        brandService.findAll().success(function (resp) {
            $scope.list = resp;
        })
    }

    $scope.findPage = function(page, size) {
        brandService.findPage(page, size).success(function(resp){
            $scope.list = resp.rows;
            $scope.paginationConf.totalItems = resp.total;
        })
    }

    // 保存、修改
    $scope.save = function() {
        brandService.save($scope.entity).success(function(resp){
            if(resp.success) {
                $scope.reloadList();
            } else {
                alert(resp.message);
            }
        });
    }

    // 查询单个品牌
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (resp) {
            if(resp) {
                $scope.entity = resp;
            }
        })
    }

    // 删除
    $scope.delete = function () {
        if($scope.selectIds.length > 0) {
            brandService.delete($scope.selectIds).success(function (resp) {
                if(resp.success) {
                    $scope.reloadList();
                    $scope.selectIds = [];
                } else {
                    alert(resp.message);
                }
            })
        } else {
            alert("请选中要删除的记录！");
        }

    }

    $scope.searchEntity = {};
    // 查询
    $scope.search = function (page, size) {
        brandService.search(page, size, $scope.searchEntity).success(function (resp) {
            $scope.list = resp.rows;
            $scope.paginationConf.totalItems = resp.total;
        });
    }

});
