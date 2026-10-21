<?php
namespace App\Http\Controllers;
use Illuminate\Http\Request;
use App\Models\User;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use Illuminate\Routing\Controller as BaseController;


// 使用 JWT 库
class AuthController extends BaseController
{
    public function createToken(){
        $key = 'yzr';
        $param=request()->param();
        $payload = [
            'iss' => '123',
            'aud' => '456',
            'iat' => time(),
            'nbf' => time()
        ];
    }
}

