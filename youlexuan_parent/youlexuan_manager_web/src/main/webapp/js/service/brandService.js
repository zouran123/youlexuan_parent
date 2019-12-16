// 定义服务
// 第一个参数：服务的名字
// 第二个参数：服务干的事情
app.service('brandService', function ($http) {
    this.findAll = function() {
        return $http.get('../brand/findAll.do');
    }

    this.findPage = function(page, size) {
        return $http.get('../brand/findPage.do?page=' + page + '&size=' + size);
    }

    this.save = function(entity) {
        var methodName = 'add';
        if(entity.id) {
            methodName = 'update';
        }
        return $http.post('../brand/' + methodName + '.do', entity);
    }

    this.findOne = function(id) {
        return $http.get('../brand/findOne.do?id=' + id);
    }

    this.delete = function (ids) {
        return $http.get('../brand/delete.do?ids=' + ids);
    }

    this.search = function (page, size, searchEntity) {
        return $http.post('../brand/search.do?page=' + page + '&size=' + size, searchEntity);
    }

    this.findBrandOptionList = function () {
        return $http.get('../brand/findBrandOptionList.do');
    }

});