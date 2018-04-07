app.controller('searchController', function ($scope,$location, searchService) {

    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        spec: {},
        'price': '',
        'pageNo': 1,
        'pageSize': 40,
        'sort': '',
        'sortField': ''
    };//搜索是发送给后台controller来接收的实体对象

    /**
     * 点击按钮的时候调用方法搜索结果
     */
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {//response=map  其中有一个属性：rows
                $scope.resultMap = response;
                //构建分页的标签
                buildPageLable();
            }
        )
    }


    buildPageLable = function () {
        $scope.pageLable = [];
        $scope.firstDot = false;
        $scope.lastDot = false;
        var firstPage = 1;
        var lastPage = $scope.resultMap.totalPages;

        if ($scope.resultMap.totalPages > 5) {
            //如果当前页<=3   展示前5页
            if ($scope.searchMap.pageNo <= 3) {
                firstPage = 1;
                lastPage = 5;
                $scope.firstDot = false;
                $scope.lastDot = true;
            } else if ($scope.searchMap.pageNo >= $scope.resultMap.totalPages - 2) {
                //如果当前页>=总页数-2    展示后5页
                firstPage = $scope.resultMap.totalPages - 4;    // 96 97 98 99 100
                lastPage = $scope.resultMap.totalPages;
                $scope.firstDot = true;
                $scope.lastDot = false;
            } else {
                // 否则：显示中间的5页   （开始页：当前页-2） 截止页：当前页+2
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
                $scope.firstDot = true;
                $scope.lastDot = true;
            }
        } else {
            // alert("总页数小于5");
            firstPage = 1;
            lastPage = $scope.resultMap.totalPages;
            $scope.firstDot = false;
            $scope.lastDot = false;
        }
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLable.push(i);
        }
    }


    $scope.searchByPage = function (pageNo) {
        pageNo = parseInt(pageNo);
        console.info(">>>>>>>>>>" + isNaN(pageNo));
        //非法的页码
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            alert("非法页码");
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }


    //排序
    $scope.searchSort = function (field, sortType) {
        $scope.searchMap.sort = sortType;
        $scope.searchMap.sortField = field;
        $scope.search();
    }

    /**
     * 判断搜索的关键字是否含有 品牌列表中有关键字 true  false
     */
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) != -1) {
                $scope.searchMap.brand=$scope.resultMap.brandList[i].text;
                return true;
            }
        }
        return false;
    }
    
    //接收从首页传递过来的关键字的值 查询
    
    $scope.loadKeyWords=function () {
        var keywords = $location.search()['keywords'];
        if(keywords!=null && keywords!=undefined){
            $scope.searchMap.keywords=keywords;
            $scope.search();
        }

    }
    
    
    

    //影响searchMap变量 ，当点击 品牌 UI规格  商品分类的时候去调用
    /**
     *
     * @param key  到底要点击的是哪一个属性（品牌  商品分类 规格）
     * @param value 传递过去的值 ：手机  平板电视
     */
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;//  var ob = {}  obj.property1=1
        }
        $scope.search();
    }

    /**
     * 删除变量的值
     * @param key
     */
    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];//javascript 的删除某一个对象中的属性
        }
        $scope.search();
    }


})