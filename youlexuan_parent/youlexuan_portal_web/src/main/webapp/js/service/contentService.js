//content服务层
app.service('contentService', function($http){
	// 查询单个实体
	this.findByCategoryId = function(catId) {
		return $http.get('../content/findByCategoryId.do?catId=' + catId);
	}

});