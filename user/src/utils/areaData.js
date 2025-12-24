class AreaDataService {
    constructor() {
        this.provinces = [];
        this.cities = {};
        this.counties = {};
        this.cache = new Map(); // 简单缓存机制
    }

    // 获取所有省份数据
    async getProvinces() {
        if (this.provinces.length > 0) return this.provinces;
        
        try {
            const response = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json');
            const data = await response.json();
            this.provinces = data.features.map(feature => ({
                code: feature.properties.adcode,
                name: feature.properties.name,
                level: 'province'
            }));
            return this.provinces;
        } catch (error) {
            console.error('获取省份数据失败:', error);
            return this.getFallbackProvinces();
        }
    }

    // 获取某个省份下的城市
    async getCities(provinceCode) {
        const cacheKey = `cities_${provinceCode}`;
        if (this.cache.has(cacheKey)) {
            return this.cache.get(cacheKey);
        }

        try {
            const response = await fetch(`https://geo.datav.aliyun.com/areas_v3/bound/${provinceCode}_full.json`);
            const data = await response.json();
            const cities = data.features.map(feature => ({
                code: feature.properties.adcode,
                name: feature.properties.name,
                level: 'city'
            }));
            this.cache.set(cacheKey, cities);
            return cities;
        } catch (error) {
            console.error('获取城市数据失败:', error);
            return this.getFallbackCities(provinceCode);
        }
    }

    // 获取某个城市下的区县
    async getCounties(cityCode) {
        const cacheKey = `counties_${cityCode}`;
        if (this.cache.has(cacheKey)) {
            return this.cache.get(cacheKey);
        }

        try {
            const response = await fetch(`https://geo.datav.aliyun.com/areas_v3/bound/${cityCode}_full.json`);
            const data = await response.json();
            const counties = data.features.map(feature => ({
                code: feature.properties.adcode,
                name: feature.properties.name,
                level: 'county'
            }));
            this.cache.set(cacheKey, counties);
            return counties;
        } catch (error) {
            console.error('获取区县数据失败:', error);
            return this.getFallbackCounties(cityCode);
        }
    }

    // 备用数据
    getFallbackProvinces() {
        return [
            { code: '110000', name: '北京市', level: 'province' },
            { code: '120000', name: '天津市', level: 'province' },
            // ... 其他省份数据
        ];
    }
}