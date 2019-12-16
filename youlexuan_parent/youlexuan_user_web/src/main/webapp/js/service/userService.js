//user服务层
app.service('userService', function($http){
	// 保存
	this.save = function(entity, myCode) {
		return $http.post('../user/add.do?myCode=' + myCode, entity);
	}

	this.sendCode = function(phone) {
		return $http.get('../user/sendCode.do?phone=' + phone);
	}

});