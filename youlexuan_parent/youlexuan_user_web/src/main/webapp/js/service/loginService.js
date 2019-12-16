//login服务层
app.service('loginService', function($http){

	this.getName = function() {
		return $http.get('../user/getName.do');
	}

});