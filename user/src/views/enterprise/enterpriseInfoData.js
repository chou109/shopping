export const rules = {
  address: [{
    required: true,
    message: "公司注册地址不能为空",
  }],
  affiliation: [{
    required: true,
    message: "所属机构不能为空",
  }],
  enterpriseNature: [{
    required: true,
    message: "企业性质不能为空",
  }],
  doe: [{
    required: true,
    message: "成立日期不能为空",
  }],
  corpn: [{
    required: true,
    message: "法人/负责人不能为空",
  }],
  corpnIdcard: [{
    required: true,
    message: "法人/负责人身份证不能为空",
  }],
  contactAddress: [{
    required: true,
    message: "联系地址不能为空",
  }],
  businessScope: [{
    required: true,
    message: "经营范围不能为空",
  }],
  // businessLicenseImage: [{
  //   required: true,
  //   message: "营业执照扫描件不能为空",
  // }],
  profile: [{
    required: true,
    message: "经营范围不能为空",
  }],
  businessLicenseGp: [{
    required: true,
    message: "营业执照有效期不能为空",
  }],
  usci: [{
    required: true,
    message: "统一社会信用代码不能为空",
  }],
  bank: [{
    required: true,
    message: "开户银行不能为空",
  }],
  addr: [{
    required: true,
    message: "开户行地址不能为空",
  }],
  registrationPhone: [{
    required: true,
    message: "注册电话不能为空",
  }],
  contactAddr: [{
    required: true,
    message: "注册地址不能为空",
  }],
  registeredCapital: [{
    required: true,
    message: "注册资本不能为空",
  }],
  paidinCapital: [{
    required: true,
    message: "实缴资本不能为空",
  }],
  industry: [{
    required: true,
    message: "行业不能为空",
  }],
}

/**
 *
 * @type {{}}
 */
export const complaintRules = {
  title: [{
    required: true,
    message: "投诉标题不能为空",
  }],
  content: [{
    required: true,
    message: "投诉信息不能为空",
  }],
  contact: [{
    required: true,
    message: "联系人不能为空",
  }],
  contactTel: [{
    required: true,
    message: "联系电话不能为空",
  }],
}

