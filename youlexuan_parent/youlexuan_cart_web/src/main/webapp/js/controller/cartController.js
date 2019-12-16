app.controller('cartController', function ($scope, cartService, addressService, orderService) {

    $scope.findCartList = function () {
        cartService.findCartList().success(function (resp) {
                if (resp) {
                    $scope.cartList = resp;
                    // 开始计算商品总数量、总价格
                    // 初始化计数变量
                    $scope.totalNum = 0;
                    $scope.totalPrice = 0.00;

                    sum($scope.cartList);
                }
            }
        );
    }

    // 改变数量
    $scope.changNum = function (skuId, num) {
        cartService.changNum(skuId, num).success(function (resp) {
                if (resp.success) {
                    $scope.findCartList();
                } else {
                    alert(resp.message);
                }
            }
        );
    }

    sum = function (list) {
        for (var i = 0; i < list.length; i++) {
            for (var j = 0; j < list[i].orderItemList.length; j++) {
                $scope.totalNum += list[i].orderItemList[j].num;
                $scope.totalPrice += list[i].orderItemList[j].totalFee;
            }
        }
    }

    $scope.findAddressList = function () {
        addressService.findAddressList().success(function (resp) {
            if (resp) {
                $scope.addressList = resp;
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault == '1') {
                        $scope.selectAddr = $scope.addressList[i];
                        break;
                    }
                }
            }
        });
    }


    // 切换地址
    $scope.changeAddr = function (addr) {
        $scope.selectAddr = addr;
    }

    // 切换付款方式
    $scope.order = {paymentType: '1'};
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    // 提交订单
    $scope.submitOrder = function() {
        $scope.order.receiverAreaName = $scope.selectAddr.address;
        $scope.order.receiver = $scope.selectAddr.contact;
        $scope.order.receiverMobile = $scope.selectAddr.mobile;

        orderService.submitOrder($scope.order).success(function(resp){
            if(resp.success) {
                if($scope.order.paymentType == '1') {
                    location.href = "pay.html";
                } else {
                    location.href = "pay_offline.html";
                }
            } else {
                alert(resp.message);
            }
        })
    }

});
