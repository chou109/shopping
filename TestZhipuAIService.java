import com.neuedu.system.demoai.ZhipuAIService;

public class TestZhipuAIService {
    public static void main(String[] args) {
        ZhipuAIService service = new ZhipuAIService();
        
        // 测试三个用户请求
        String request1 = "有没有推荐的黄金首饰";
        String request2 = "有没有推荐的钻石彩宝";
        String request3 = "有没有推荐的品质男鞋";
        
        System.out.println("测试请求1: " + request1);
        System.out.println("是否为商品推荐请求: " + service.isProductRecommendationRequest(request1));
        System.out.println("提取的分类关键词: " + service.extractCategoryKeywordsFromQuestion(request1));
        System.out.println();
        
        System.out.println("测试请求2: " + request2);
        System.out.println("是否为商品推荐请求: " + service.isProductRecommendationRequest(request2));
        System.out.println("提取的分类关键词: " + service.extractCategoryKeywordsFromQuestion(request2));
        System.out.println();
        
        System.out.println("测试请求3: " + request3);
        System.out.println("是否为商品推荐请求: " + service.isProductRecommendationRequest(request3));
        System.out.println("提取的分类关键词: " + service.extractCategoryKeywordsFromQuestion(request3));
    }
}