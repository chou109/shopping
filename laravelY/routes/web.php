<?php

use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/
//Route::get('posts/{text}',['\App\Http\Controllers\PostController','show']);
Route::get('posts/index',"PostController@index");
//Route::get('/', function () {
//    $id=request('id');
//    return  view('text',['id'=>$id]);
//    return view('welcome');
//});
