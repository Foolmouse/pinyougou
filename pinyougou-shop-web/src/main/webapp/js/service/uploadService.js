//文件上传服务层
app.service("uploadService",function($http){
    /**
     * anjularjs对于post和get请求默认的Content-Type header
     * 是application/json。通过设置‘Content-Type’: undefined，
     * 这样浏览器会帮我们把Content-Type 设置为 multipart/form-data.
     *
     * 通过设置 transformRequest: angular.identity ，
     * anjularjs transformRequest function 将序列化我们的formdata object.
     * @returns {*}
     */
    this.uploadFile=function(){

        var formData=new FormData();
        //第一个参数：file和controller中的参数一致
        // 第二个参数：file 和input 中的id一致 这里支持多图片，现在只要一个所以就选一张即可.
        formData.append("file",file.files[0]);
        return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        });
    }


    /*this.uploadFile = function () {
        var formData = new FormData();
        //第一个参数：file和controller中的参数一致
        // 第二个参数：file 和input 中的id一致 这里支持多图片，现在只要一个所以就选一张即可.
        formData.append("file",file.files[0])
        return $http({
            method:'post',
            url:'../upload.do',
            data:formData,
            headers:{'Content-Type':undefined},
            transformRequest:angular.identity
        })

    }*/


});