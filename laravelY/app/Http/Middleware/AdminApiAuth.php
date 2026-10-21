<?php

namespace App\Http\Middleware;
use Closure;
use Illuminate\Http\Request;
//use Symfony\Component\HttpFoundation\Response;

/**
 * 后台权限验证
 */
class AdminApiAuth
{
    public function handle(Request $request, Closure $next)
    {
//    exit("454654");
        return $next($request);
    }
}
