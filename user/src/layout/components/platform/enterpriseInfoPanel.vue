<template>

  <div class="content">
    <el-row>
      <el-col :span="8">
        <span class="label">企业名称：</span>
        <span class="text">{{ enterpriseData.name }}</span>
      </el-col>
      <el-col :span="16">
        <span class="label">统一社会信用代码：</span>
        <span class="text">{{ enterpriseData.usci }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <span class="label">公司注册地址：</span>
        <span class="text">{{ enterpriseData.address }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <span class="label">机构类型：</span>
        <span class="text">{{ getDictValue(enterpriseData.affiliation, 'enterpriseAffiliation') }}</span>
      </el-col>
      <el-col :span="8">
        <span class="label">企业性质：</span>
        <span class="text">{{ getDictValue(enterpriseData.enterpriseNature, 'enterpriseNature') }}</span>
      </el-col>
      <el-col :span="8">
        <span class="label">成立日期：</span>
        <span class="text">{{ parseTime(enterpriseData.doe, '{y}年{m}月{d}日') }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <span class="label">法人/负责人：</span>
        <span class="text">{{ enterpriseData.corpn }}</span>
      </el-col>
      <el-col :span="16">
        <span class="label">法人/负责人身份证：</span>
        <span class="text">{{ enterpriseData.corpnIdcard }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <span class="label">法人/负责人身份证扫描件：</span>
        <span class="text"><el-button type="text" style="color: #17C2AA;padding-top: 0"
            @click="showImage(enterpriseData.corpnIdcardImage, '法人/负责人身份证扫描件')">查看图片</el-button></span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <span class="label">单位联系地址：</span>
        <span class="text">{{ enterpriseData.contactAddress }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <span class="label">经营范围：</span>
        <span class="text">{{ enterpriseData.businessScope }}</span>
      </el-col>
      <el-col :span="16">
        <span class="label">公司简介：</span>
        <span class="text">{{ enterpriseData.profile }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <span class="label">营业执照有效期：</span>
        <span class="text">{{ enterpriseData.businessLicenseGp }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <span class="label">营业执照扫描件：</span>
        <span class="text"><el-button type="text" style="color: #17C2AA;padding-top: 0"
            @click="showImage(enterpriseData.businessLicenseImage, '营业执照扫描件')">查看图片</el-button></span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <span class="label">开户行：</span>
        <span class="text">{{ enterpriseData.bank }}</span>
      </el-col>
      <el-col :span="16">
        <span class="label">银行账号：</span>
        <span class="text">{{ enterpriseData.account }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <span class="label">开户行地址：</span>
        <span class="text">{{ enterpriseData.addr }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <span class="label">单位注册电话：</span>
        <span class="text">{{ enterpriseData.registrationPhone }}</span>
      </el-col>
      <el-col :span="16">
        <span class="label">单位注册地址：</span>
        <span class="text">{{ enterpriseData.contactAddr }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="8">
        <span class="label">注册资本（万元）：</span>
        <span class="text">{{ enterpriseData.registeredCapital }}</span>
      </el-col>
      <el-col :span="16">
        <span class="label">实缴资本：</span>
        <span class="text">{{ enterpriseData.paidinCapital }}</span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <span class="label">开户许可证/基本户存款信息证：</span>
        <span class="text"><el-button type="text" style="color: #17C2AA;padding-top: 0"
            @click="showImage(enterpriseData.accountLicence, '开户许可证/基本户存款信息证')">查看图片</el-button></span>
      </el-col>
    </el-row>
    <el-row v-if="showUpload">
      <el-col :span="24">
        <span class="label">上传文件：</span>
        <span class="text">
          <fileUpload :file-type="fileType" :limit="limit" :file-size="size" :is-show-file-list="true"
            :is-show-tip="true" v-model="fileList" style="margin-left: 0px" />
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <div style="width: 1200px;display: flex;flex-direction: row-reverse;">
          <el-button style="background: #17C2AA;width: 72px;height: 32px;border-radius: 4px;color: #FFFFFF;"
            @click="handleUpdate">{{ textValue }}
          </el-button>
        </div>
      </el-col>
    </el-row>
    <el-dialog v-model="imageVisible" :title="`${imageVisibleTitle}`" width="800px" append-to-body :closed="closeImage">
      <el-image :src="imageUrl" style="display: block; max-width: 100%; min-height: 300px; margin: 0 auto;">
        <div slot="placeholder" class="image-slot">
          加载中<span class="dot">...</span>
        </div>
      </el-image>
    </el-dialog>
  </div>

</template>

<script>
import { parseTime } from "@/utils/neu";
import { getBaseUrl } from '@/utils/env'
import ImageUpload from '@/components/ImageUpload'
import FileUpload from '@/components/FileUpload'

export default {
  name: "EnterpriseInfoPanel",
  props: ['dataSource', 'text', 'isShowUpload', 'type', 'limit', 'size'],
  components: { ImageUpload, FileUpload },
  data() {
    return {
      enterpriseData: {},
      imageUrl: "",
      imageVisible: false,
      imageVisibleTitle: '',
      enterpriseAffiliation: [],
      enterpriseNature: [],
      showUpload: false,
      textValue: "",
      fileList: [],
      fileType: ['pdf', 'zip', 'rar', 'png', 'jpg', 'jpeg'],
      fileLimit: 10,
      fileSize: 100,
    }
  },
  created() {
    this.textValue = this.text
    this.showUpload = this.isShowUpload
    this.fileType = this.type
    this.fileLimit = this.limit
    this.fileSize = this.size
    this.getDicts('enterprise_affiliation').then(response => {
      this.enterpriseAffiliation = response.data
    })
    this.getDicts('enterprise_nature').then(response => {
      this.enterpriseNature = response.data
    })
  },
  methods: {
    parseTime,
    getBaseUrl,
    closeImage() {
      this.imageVisible = !this.imageVisible
    },
    handleUpdate() {
      const that = this
      this.enterpriseData['files'] = that.fileList
      this.$emit('handle-update', that.enterpriseData)
    },
    showImage(url, title) {
      this.imageUrl = url;
      this.imageVisible = true;
      this.imageVisibleTitle = title;
    },
    getDictValue(key, type) {
      const value = this[type].filter(x => x.dictValue === key);
      if (value != null && value.length > 0) {
        return value[0].dictLabel;
      }
      return ''
    }
  },
  watch: {
    dataSource(value) {
      this.enterpriseData = value
    }
  }

}
</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
}

.el-col {
  display: flex;
}
</style>
