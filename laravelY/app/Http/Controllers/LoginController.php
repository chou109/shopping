<?php

namespace App\Http\Controllers;

use App\Models\User;
use Firebase\JWT\JWT;
use Illuminate\Http\Request;
use Illuminate\Routing\Route;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Tymon\JWTAuth\Contracts\Providers\Auth;
use Tymon\JWTAuth\Facades\JWTAuth;


class LoginController
{
    public  function admin(Request  $request)
    {
        $validator = Validator::make($request->all(), [
            'userName' => 'required',
            'password' => 'required',
        ],[
            'userName.required'=>'用户名必填',
            'password.required'=>'密码必填'
        ]);
        if ($validator->fails()) {
            return [
                "code"=>400,
                "status"=>40000,
                "message"=>array_values($validator->errors()->messages())[0][0],
            ];
        }
        $password=md5($request['password']);
        $yz= DB::table('user')->where(['user_name'=>$request['userName'],'password'=>$password,'delete_flag'=>0])->get();
//        return $yz;
//        $user = \Illuminate\Support\Facades\DB::table('user')->where(['user_name'=>"admin",'password'=>"e10adc3949ba59abbe56e057f20f883e"])->get();
        // 秘钥，实际使用时记得设置复杂些。
        $key = "hello";
        // 元数据
        $payload = array(
            // 用户 id 用来解 token 时所需要的关键信息。
            'user_id' => $yz[0]->id,
            'user_name' => $yz[0]->user_name,
            // token 过期时间，这里设置一个小时。
            'exp' => time() + 3600
            // 你还可以添加任意元数据
        );
        // 生成 token
        $jwt = JWT::encode($payload, $key, 'HS256');
//        return response([
//            'token' => $jwt
//        ]);
        $request->session()->put('token',$jwt);
        if(count($yz)!=0){
            $result=[
                "code"=>200,
                "status"=>20000,
                "data"=>$yz->toArray(),
                "Authorization"=>$jwt
            ];
            return  response()->json($result,$result['code']);
        }else{
            return "用户名或密码错误";
        }
//        if(strcmp(md5($request['password']),"e10adc3949ba59abbe56e057f20f883e")==0){
    }
}
