package org.pjm2.report.internal;

import javax.persistence.EntityManager;


public class WeixinDao extends AbstractDAO {
	public WeixinDao(EntityManager m) {
		super(m);
	}
	
	protected String getFiledsSql() {
		return "select n.id, n.image_date, t.column_name, f.body from weixins as n, weixin_fields as f, weixin_classifieds as c, templates as t where " +
				" n.id in (%s) and n.id = f.weixins_id and f.weixin_classifieds_id = c.id and c.template_id = t.id order by n.image_date  ASC";
	}

	protected String getItemByClassifieldSql() {
		return "select n.id from weixins as n where n.classified = '%s' ";
	}

	protected String getItemByDateSql() {
		return "select n.id from weixins as n, weixin_fields as f, weibo_classifieds as c where n.id = f.weixins_id and f.weixin_classifieds_id = c.id and c.id = %d and n.classified = '%s' ";
	}

	
	protected String getDateFieldSql() {
		return "select c.id from weixin_classifieds as c, templates as t where c.classified = '%s' and c.template_id = t.id and t.column_name='日期'";
	}

	@Override
	protected String getColumnNameSql() {
		return "select t.column_name from weixin_classifieds as c, templates as t where c.classified = '%s' and c.template_id = t.id ";
	}
}
