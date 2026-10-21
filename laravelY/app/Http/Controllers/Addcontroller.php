<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class Addcontroller
{
    public  function add($id){
        return DB::table('user_address')->where(['delete_flag'=>0,'create_user'=>$id])->get();
    }

    public  function del($id){
        return DB::table('user_address')->where(['id'=>$id])->update(["delete_flag"=>1]);
    }
    public  function address($id,Request $request){
        $data=$request->all();
        $data['create_user']=$id;
        if( $request['id']===$id){
            return  DB::table('user_address')->insert($data);
        }else{
            return  DB::table('user_address')->where(['id'=>$request['id']])->update(["receive_name"=>$request['receive_name'],
                'area'=>$request['area'],'city'=>$request['city'],'province'=>$request['province'],'receive_phone'=>$request['receive_phone'],
                'street'=>$request['street'],'postal_code'=>$request['postal_code']]);
        }
    }
}
