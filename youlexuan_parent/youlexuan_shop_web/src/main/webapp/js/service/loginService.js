// 登录服务
app.service('loginService', function ($http) {
    this.getName = function() {
        return $http.get('../login/getName.do');
    }

});