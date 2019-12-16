//cart服务层
app.service('cartService', function($http){

	this.findCartList = function(searchMap) {
		return $http.get('cart/findCartList.do');
	}

	this.changNum = function(skuId, num) {
		return $http.get('cart/addGoodsToCartList.do?skuId=' + skuId + '&num=' + num);
	}


});