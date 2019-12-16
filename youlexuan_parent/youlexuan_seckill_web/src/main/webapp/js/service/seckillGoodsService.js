//seckill_goods服务层
app.service('seckillGoodsService', function($http){
	// 查询列表
	this.findList = function() {
		return $http.post('seckillGoods/findList.do');
	}

	this.findOne = function(sekKillId) {
		return $http.get('seckillGoods/findOne.do?sekKillId=' + sekKillId);
	}

});