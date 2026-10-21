<?php

namespace App\Http\Controllers;


use App\User;
use http\Env\Request;
use Illuminate\Support\Facades\DB;

class UserController
{
    public  function user($id){
        return DB::table('user')->where(['id'=>$id])->get();
    }

    public function userPage(\Illuminate\Http\Request $request){
//        return  $request;
        return DB::table('user')->where(['delete_flag'=>0])->paginate($request['pageSize'])->toArray();
    }

    public function userDelete($id){
        return DB::table('user')->where(['id'=>$id])->update(['delete_flag'=>1]);
    }

    public function userUpdate(\Illuminate\Http\Request $request){
//        return $request;
        $data=$request->all();
        return DB::table('user')->where(['id'=>$data['id']])->update($data);
    }

    public function  userGet(){
        return DB::table('user')->where(['delete_flag'=>0])->get();
    }


    public function banRe($id,\Illuminate\Http\Request $request){

//return $request['balance'];
        $data=$request->all();
//        return  $data;
    return DB::table('user')->where(['id'=>$id])->update(['balance'=>$data['balance']]);

    }







    public  function add(){
        return view('text');
    }
    public  function store(\Illuminate\Http\Request $request)
    {
        return 123;
        $input=$request->all();
        $input['password']=md5($input['password']);
        dd($input);
//    User::created(['username'=>$input['username'],'password'=>$input['password']]);
        $res=User::created($input);
        dd($res);
        return view('text');

    }
    public  function ssss(\Illuminate\Http\Request $request )
    {
        return 123;
//        $data=$testRequest->only(['title','body']);
        $data=$request->all();
//        $list= DB::table('blog_user')->paginate($data['limit'])->toArray();
//        return $this->paginate(2);
//        return $list['data'];
//        return [
//            'data'=>$list['data'],
//            'total'=>$list['total']
//        ];
        return DB::table('blog_user')->insert($data);
        return DB::table('blog_user')->where(['id'=>3])->update($data);
//        return DB::table('blog_user')->delete(2);
//        return DB::table('blog_user')->where('id','>','2')->get();

    }
}
