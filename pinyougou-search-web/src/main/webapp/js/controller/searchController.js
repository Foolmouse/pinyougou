app.controller('searchController',function ($scope,searchService) {

    $scope.searchMap={'keywords':'','category':'','brand':'',spec:{}};//搜索是发送给后台controller来接收的实体对象

    /**
     * 点击按钮的时候调用方法搜索结果
     */
    $scope.search=function () {
        searchService.search($scope.searchMap).success(
            function (response) {//response=map  其中有一个属性：rows
                $scope.resultMap=response;
            }
        )
    }

    //影响searchMap变量 ，当点击 品牌 UI规格  商品分类的时候去调用
    /**
     *
     * @param key  到底要点击的是哪一个属性（品牌  商品分类 规格）
     * @param value 传递过去的值 ：手机  平板电视
     */
    $scope.addSearchItem=function (key,value) {
        if(key=='category'|| key=='brand'){
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;//  var ob = {}  obj.property1=1
        }
        $scope.search();
    }

    /**
     * 删除变量的值
     * @param key
     */
    $scope.removeSearchItem=function (key) {
        if(key=='category'|| key=='brand'){
            $scope.searchMap[key]='';
        }else{
            delete $scope.searchMap.spec[key];//javascript 的删除某一个对象中的属性
        }
        $scope.search();
    }

})