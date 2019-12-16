app.controller('loginController', function($scope, loginService){
    $scope.getName = function() {
        loginService.getName().success(function(resp){
            // 正则表达式
            $scope.loginName = resp.replace(/\"/g, "");
        })
    }
});
