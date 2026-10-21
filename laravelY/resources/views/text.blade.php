
<!DOCTYPE html>
<html>
<head>
    <title></title>
</head>
<body>

<form action="{{url('api/user/store')}}" method="post">
    <table>
        <tr>
<!--            <input type="hidden" name="_token" value="{{ csrf_token() }}">-->
            <td>用户名</td>
            <td><input type="text" name="username"></td>
        </tr>
        <tr>
            <td>密码</td>
            <td><input type="password" name="password"></td>
        </tr>
        <tr>
            <td></td>
            <td><input type="submit" value="提交"></td>
        </tr>
    </table>

</form>
</body>
</html>
