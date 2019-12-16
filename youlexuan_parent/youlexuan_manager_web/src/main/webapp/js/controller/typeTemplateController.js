//type_template控制层 
app.controller('typeTemplateController', function ($scope, $controller, typeTemplateService, brandService, specificationService) {

    // 继承
    $controller("baseController", {
        $scope: $scope
    });

    // 保存
    $scope.save = function () {
        typeTemplateService.save($scope.entity).success(function (response) {
            if (response.success) {
                // 重新加载
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        });
    }

    //查询实体
    $scope.findOne = function (id) {
        typeTemplateService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                // 将字符串类型的json格式转为json对象
                $scope.entity.brandIds = JSON.parse($scope.entity.brandIds);
                $scope.entity.specIds = JSON.parse($scope.entity.specIds);
                $scope.entity.customAttributeItems = JSON.parse($scope.entity.customAttributeItems);
            }
        );
    }

    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        typeTemplateService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                    $scope.selectIds = [];
                }
            }
        );
    }

    // 定义搜索对象
    $scope.searchEntity = {};
    // 搜索
    $scope.search = function (page, size) {
        typeTemplateService.search(page, size, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        );
    }

    // 品牌下拉列表的模拟数据
    //$scope.brandList = {data:[{"id":1,"text":"测试1"},{"id":2,"text":"测试2"},{"id":3,"text":"测试3"}]};
    $scope.findBrandOptionList = function () {
        brandService.findBrandOptionList().success(function (resp) {
            if (resp) {
                $scope.brandList = {data: resp};
            }
        })
    }
    // 规格下拉列表的模拟数据
    // $scope.specList = {data:[{"id":4,"text":"测试4"},{"id":5,"text":"测试5"},{"id":6,"text":"测试6"}]};
    $scope.findSpecOptionList = function () {
        specificationService.findSpecOptionList().success(function (resp) {
            if (resp) {
                $scope.specList = {data: resp};
            }
        })
    }

    // 增加自定义属性的表格行
    $scope.addTableRow = function () {
        $scope.entity.customAttributeItems.push({});
    }

    // 删除自定义属性的表格行
    $scope.delTableRow = function (idx) {
        $scope.entity.customAttributeItems.splice(idx, 1);
    }

});	
