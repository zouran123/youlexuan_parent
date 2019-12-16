app.controller('payController', function ($scope, $location, payService) {

    $scope.createNative = function() {
        payService.createNative().success(function(resp){
            if(resp) {
                // 订单号
                $scope.tradeNo = resp.out_trade_no;
                // 支付金额
                $scope.money = resp.total_fee;

                var qr = new QRious({
                    element: document.getElementById('mycode'),
                    size: 250,
                    level: 'H',
                    value: resp.qrcode
                })

                // 检查支付状态
                queryPayStatus(resp.out_trade_no);
            }
        });
    }


    queryPayStatus = function(out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (resp) {
            if(resp.success) {
                location.href = 'paysuccess.html#?payMoney=' + $scope.money;
            } else {
                if(resp.message == '二维码超时') {
                    $(".red").html("二维码已过期，刷新页面重新获取二维码");
                } else {
                    location.href = 'payfail.html';
                }
            }
        });
    }


    $scope.initMoney = function() {
        $scope.payMoney = $location.search()['payMoney'];
    }

});
