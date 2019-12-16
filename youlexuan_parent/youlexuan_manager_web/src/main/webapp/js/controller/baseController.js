app.controller('baseController', function ($scope) {
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 5,
        perPageOptions: [5, 10, 20, 30],
        // 进入页面以及每次更改当前页就自动调用
        onChange: function () {
            //切换页码，重新加载
            $scope.reloadList();
        }
    };

    // 删除
    $scope.selectIds = [];//选中的ID数组
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            // 选中
            $scope.selectIds.push(id);
        } else {
            // 移除
            var idx = $scope.selectIds.indexOf(id);
            // 第一个参数：要删除元素的索引位置
            // 第二参数：删除的个数
            $scope.selectIds.splice(idx, 1);
        }
    }

    //  刷新页面
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    // 全选
    $scope.selectAll = function ($event) {
        var state = $event.target.checked;
        $(".eachbox").each(function (idx, obj) {
            obj.checked = state;
            var id = parseInt($(obj).parent().next().text());
            if (state) {
                $scope.selectIds.push(id);
            } else {
                var idx = $scope.selectIds.indexOf(id);
                $scope.selectIds.splice(idx, 1);
            }
        })
    }

    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToStr = function (jsonString, key) {
        var json = JSON.parse(jsonString);//将json字符串转换为json对象
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ","
            }
            value += json[i][key];
        }
        return value;
    }

});
