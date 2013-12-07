/*
XOWA: the XOWA Offline Wiki Application
Copyright (C) 2012 gnosygnu@gmail.com

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package gplx.xowa; import gplx.*;
import gplx.ios.*; 
import gplx.xowa.apps.*; import gplx.xowa.apps.caches.*; import gplx.xowa.specials.*;
import gplx.xowa.wikis.*; import gplx.xowa.users.*; import gplx.xowa.cfgs.*; import gplx.xowa.ctgs.*; import gplx.xowa.html.tocs.*; import gplx.xowa.fmtrs.*; 
import gplx.xowa.xtns.*; import gplx.xowa.xtns.scribunto.*; import gplx.xowa.xtns.math.*;	
import gplx.xowa.parsers.logs.*;
public class Xoa_app implements GfoInvkAble {
	public Xoa_app(Gfo_usr_dlg_xowa usr_dlg, Io_url root_dir, Io_url user_dir, String bin_dir_name) {
		this.usr_dlg = usr_dlg;
		log_wtr = usr_dlg.Log_wtr();
		cfg_mgr = new Xoa_cfg_mgr(this);
		url_cmd_eval = new Apps_app_mgr_eval(this);
		fsys_mgr = new Apps_fsys_mgr(this, root_dir, bin_dir_name);
		user = new Xou_user(this, user_dir);
		log_wtr.Log_dir_(user.Fsys_mgr().App_temp_dir().GenSubDir("log"));
		fsys_mgr.Temp_dir_(user.Fsys_mgr().App_temp_dir());
		lang_mgr = new Xoa_lang_mgr(this);
		wiki_mgr = new Xoa_wiki_mgr(this);
		gui_mgr = new Xoa_gui_mgr(this);
		bldr = new Xob_bldr(this);
		file_mgr.Init_app(this, usr_dlg);
		href_parser = new Xoh_href_parser(url_converter_href, url_parser.Url_parser());
		sanitizer = new Xop_sanitizer(amp_trie, msg_log);
		user_mgr = new Xou_user_mgr(this, user);
		sys_cfg = new Xoa_sys_cfg(this);
		url_alias_mgr = new Xoa_url_alias_mgr(this);
		cur_redirect = new Xoa_cur(this);
		shell = new Xoa_shell(this);
		setup_mgr = new Xoi_setup_mgr(this);
		gfs_mgr = new Xoa_gfs_mgr(this);
		wiki_cfg_bldr = new Xoac_wiki_cfg_bldr_mgr(this);
		xtn_mgr = new Xow_xtn_mgr().Ctor_by_app(this);
		hive_mgr = new Xoa_hive_mgr(this);
		Io_url.Http_file_str_encoder = url_converter_fsys;
		server.App_ctor(this);
		fmtr_mgr = new Xoa_fmtr_mgr(this);
		log_mgr = new Xop_log_mgr(this);
	}
	public NumberParser Utl_num_parser() {return utl_num_parser;} private NumberParser utl_num_parser = new NumberParser();
	public void Init() {
		log_wtr.Init();
		gui_mgr.Init();
		fsys_mgr.Init();
		user.App_init();
		file_mgr.App_init();
		wiki_mgr.App_init();
		gplx.xowa.utls.upgrades.Xoa_upgrade_mgr.Check(this);
		stage = Xoa_stage_.Tid_init_done;
		ctg_mgr.Init_by_app(this);
		setup_mgr.Init_by_app(this);
	}
	public boolean Launch_done() {return stage == Xoa_stage_.Tid_launch_done;}
	public void Launch() {
		if (Launch_done()) return;
		gplx.xowa.users.prefs.Prefs_converter._.Check(this);
		stage = Xoa_stage_.Tid_launch_done;
	}
	public byte Stage() {return stage;} public Xoa_app Stage_(byte v) {stage = v; return this;} private byte stage = Xoa_stage_.Tid_ctor_done;
	public boolean Term_cbk() {
		if (setup_mgr.Cmd_mgr().Working()) {
			if (!gui_mgr.Kit().Ask_yes_no("", "", "An import is in progress. Are you sure you want to exit?")) return false;
		} 
		gui_mgr.Main_win().Gui_wtr().Canceled_y_();
		user.App_term();
		log_wtr.Term();
		log_mgr.Rls();
		if (Scrib_engine.Engine() != null) Scrib_engine.Engine().Term();
		wiki_mgr.Rls();
		return true;
	}
	public Xoa_wiki_mgr			Wiki_mgr() {return wiki_mgr;} private Xoa_wiki_mgr wiki_mgr;
	public Xou_user_mgr			User_mgr() {return user_mgr;} private Xou_user_mgr user_mgr;
	public Xof_file_mgr			File_mgr() {return file_mgr;} private Xof_file_mgr file_mgr = new Xof_file_mgr();
	public Xoa_lang_mgr			Lang_mgr() {return lang_mgr;} private Xoa_lang_mgr lang_mgr;
	public Xoa_gui_mgr			Gui_mgr() {return gui_mgr;} private Xoa_gui_mgr gui_mgr;
	public Xou_user				User() {return user;} private Xou_user user;
	public Xob_bldr				Bldr() {return bldr;} private Xob_bldr bldr;
	public Xow_xtn_mgr			Xtn_mgr() {return xtn_mgr;} private Xow_xtn_mgr xtn_mgr;
	public Xop_tkn_mkr			Tkn_mkr() {return tkn_mkr;} private Xop_tkn_mkr tkn_mkr = new Xop_tkn_mkr();
	public Gfo_usr_dlg			Usr_dlg() {return usr_dlg;} private Gfo_usr_dlg usr_dlg;
	public Gfo_log_wtr			Log_wtr() {return log_wtr;} private Gfo_log_wtr log_wtr;
	public Xoa_gfs_mgr			Gfs_mgr() {return gfs_mgr;} private Xoa_gfs_mgr gfs_mgr;
	public Xoa_special_mgr		Special_mgr() {return special_mgr;} private Xoa_special_mgr special_mgr = new gplx.xowa.specials.Xoa_special_mgr();
	public Xop_log_mgr			Log_mgr() {return log_mgr;} private Xop_log_mgr log_mgr;

	public Apps_fsys_mgr		Fsys_mgr() {return fsys_mgr;} private Apps_fsys_mgr fsys_mgr;
	public Xoa_hive_mgr			Hive_mgr() {return hive_mgr;} private Xoa_hive_mgr hive_mgr;
	public Xoa_url_parser		Url_parser() {return url_parser;} private Xoa_url_parser url_parser = new Xoa_url_parser();
	public Xoh_href_parser		Href_parser() {return href_parser;} private Xoh_href_parser href_parser;
	public Xop_sanitizer		Sanitizer() {return sanitizer;} private Xop_sanitizer sanitizer;
	public ByteTrieMgr_slim		AmpTrie() {return amp_trie;} private ByteTrieMgr_slim amp_trie = Xop_amp_trie._;
	public ByteTrieMgr_fast		TmplBgnTrie() {return tmplBgnTrie;} private ByteTrieMgr_fast tmplBgnTrie = Xop_curly_bgn_lxr.tmpl_bgn_trie_();
	public ByteTrieMgr_slim		TblwWsTrie() {return tblwWsTrie;} private ByteTrieMgr_slim tblwWsTrie = Xop_tblw_ws_itm.trie_();
	public Xop_xatr_parser		Xatr_parser() {return xatr_parser;} private Xop_xatr_parser xatr_parser = new Xop_xatr_parser();
	public Xop_xnde_tag_regy	Xnde_tag_regy() {return xnde_tag_regy;} private Xop_xnde_tag_regy xnde_tag_regy = new Xop_xnde_tag_regy();
	public Xof_math_subst_regy	Math_subst_regy() {return math_subst_regy;} private Xof_math_subst_regy math_subst_regy = new Xof_math_subst_regy();
	public Xog_win_wtr			Gui_wtr() {return gui_mgr.Main_win().Gui_wtr();}

	public Xop_toc_mgr			Toc_mgr() {return toc_mgr;} private Xop_toc_mgr toc_mgr = new Xop_toc_mgr();
	public Xoi_setup_mgr		Setup_mgr() {return setup_mgr;} private Xoi_setup_mgr setup_mgr;
	public Gfo_msg_log			Msg_log() {return msg_log;} private Gfo_msg_log msg_log = new Gfo_msg_log(Xoa_app_.Name);
	public Gfo_msg_log			Msg_log_null() {return msg_log_null;} private Gfo_msg_log msg_log_null = new Gfo_msg_log("null_log");

	public Url_encoder			Url_converter_id()			{return url_converter_id;} Url_encoder url_converter_id = Url_encoder.new_html_id_();
	public Url_encoder			Url_converter_url()			{return url_converter_url;} Url_encoder url_converter_url = Url_encoder.new_http_url_();
	public Url_encoder			Url_converter_url_ttl()		{return url_converter_url_ttl;} Url_encoder url_converter_url_ttl = Url_encoder.new_http_url_ttl_();
	public Url_encoder			Url_converter_href()		{return url_converter_href;} Url_encoder url_converter_href = Url_encoder.new_html_href_mw_();
	public Url_encoder			Url_converter_comma()		{return url_converter_comma;} Url_encoder url_converter_comma = Url_encoder.url_comma();
	public Url_encoder			Url_converter_gfs()			{return url_converter_gfs;} Url_encoder url_converter_gfs = Url_encoder.new_gfs_();
	public Url_encoder			Url_converter_fsys()		{return url_converter_fsys;} Url_encoder url_converter_fsys = Url_encoder.new_fsys_lnx_();
	public Url_encoder			Url_converter_fsys_safe()	{return url_converter_fsys_safe;} Url_encoder url_converter_fsys_safe = Url_encoder.new_fsys_wnt_();
	public Xoh_file_main_wkr	File_main_wkr() {return file_main_wkr;} private Xoh_file_main_wkr file_main_wkr = new Xoh_file_main_wkr();		
	public Bry_bfr_mkr			Utl_bry_bfr_mkr() {return utl_bry_bfr_mkr;} Bry_bfr_mkr utl_bry_bfr_mkr = new Bry_bfr_mkr();
	public Gfo_fld_rdr			Utl_fld_rdr() {return utl_fld_rdr;} Gfo_fld_rdr utl_fld_rdr = Gfo_fld_rdr.xowa_();
	public Gfo_log_bfr			Log_bfr() {return log_bfr;} private Gfo_log_bfr log_bfr = new Gfo_log_bfr();
	public gplx.xowa.html.utils.Xoh_js_cleaner Utl_js_cleaner() {return utl_js_cleaner;} gplx.xowa.html.utils.Xoh_js_cleaner utl_js_cleaner = new gplx.xowa.html.utils.Xoh_js_cleaner();
	public HashAdp				Tmpl_result_cache() {return tmpl_result_cache;} HashAdp tmpl_result_cache = HashAdp_.new_bry_();
	public Xoa_sys_cfg			Sys_cfg() {return sys_cfg;} private Xoa_sys_cfg sys_cfg;
	public Xoa_url_alias_mgr	Url_alias_mgr() {return url_alias_mgr;} private Xoa_url_alias_mgr url_alias_mgr;
	public ByteAryFmtr			Tmp_fmtr() {return tmp_fmtr;} ByteAryFmtr tmp_fmtr = ByteAryFmtr.new_("");
	public boolean					Xwiki_missing(byte[] wiki_key)	{return user.Wiki().Xwiki_mgr().Get_by_key(wiki_key) == null;} // NOTE: only the user_wiki has a full list of all wikis b/c it has xwiki objects; wiki_mgr does not, b/c it has heavier wiki objects which are loaded dynamically;
	public boolean					Xwiki_exists(byte[] wiki_key)	{return user.Wiki().Xwiki_mgr().Get_by_key(wiki_key) != null;}
	public Xoa_ctg_mgr			Ctg_mgr() {return ctg_mgr;} private Xoa_ctg_mgr ctg_mgr = new Xoa_ctg_mgr();
	public Apps_app_mgr_eval Url_cmd_eval() {return url_cmd_eval;} Apps_app_mgr_eval url_cmd_eval;
	public Xoac_wiki_cfg_bldr_mgr Cfg_wiki_bldr() {return wiki_cfg_bldr;} private Xoac_wiki_cfg_bldr_mgr wiki_cfg_bldr;
	public Xoa_cur Cur_redirect() {return cur_redirect;} private Xoa_cur cur_redirect;
	public Xoa_cfg_mgr			Cfg_mgr() {return cfg_mgr;} private Xoa_cfg_mgr cfg_mgr;
	public Io_stream_zip_mgr	Zip_mgr() {return zip_mgr;} Io_stream_zip_mgr zip_mgr = new Io_stream_zip_mgr();
	public gplx.xowa.html.Xoh_html_mgr Html_mgr() {return html_mgr;} private gplx.xowa.html.Xoh_html_mgr html_mgr = new gplx.xowa.html.Xoh_html_mgr();
	public gplx.xowa.servers.Xosrv_server Server() {return server;} gplx.xowa.servers.Xosrv_server server = new gplx.xowa.servers.Xosrv_server();
	public gplx.xowa.servers.Xosrv_webserver Webserver() {return webserver;} gplx.xowa.servers.Xosrv_webserver webserver = new gplx.xowa.servers.Xosrv_webserver();
	public Xoa_cache_mgr Cache_mgr() {return cache_mgr;} private Xoa_cache_mgr cache_mgr = new Xoa_cache_mgr();
	private Xoa_shell shell; private Xoa_fmtr_mgr fmtr_mgr;
	public void Reset_all() {
		this.Free_mem(true);
		gplx.xowa.xtns.scribunto.Scrib_engine.Engine_invalidate();
		Env_.GarbageCollect();
	}
	public void Free_mem(boolean clear_ctx) {
		tmpl_result_cache.Clear();
		utl_bry_bfr_mkr.Clear();
		msg_log.Clear();
		wiki_mgr.Free_mem(clear_ctx);
	}
	public Object Invk(GfsCtx ctx, int ikey, String k, GfoMsg m) {
		if		(ctx.Match(k, Invk_gui))					return gui_mgr;
		else if	(ctx.Match(k, Invk_bldr))					return bldr;
		else if	(ctx.Match(k, Invk_wikis))					return wiki_mgr;
		else if (ctx.Match(k, Invk_fsys))					return fsys_mgr;
		else if	(ctx.Match(k, Invk_files))					return file_mgr;
		else if (ctx.Match(k, Invk_langs))					return lang_mgr;
		else if (ctx.Match(k, Invk_users))					return user_mgr;
		else if (ctx.Match(k, Invk_user))					return user;
		else if (ctx.Match(k, Invk_sys_cfg))				return sys_cfg;
		else if	(ctx.Match(k, Invk_url_aliases))			return url_alias_mgr;
		else if	(ctx.Match(k, Invk_cur))					return cur_redirect;
		else if	(ctx.Match(k, Invk_html))					return html_mgr;
		else if	(ctx.Match(k, Invk_shell))					return shell;
		else if	(ctx.Match(k, Invk_log))					return log_wtr;
		else if	(ctx.Match(k, Invk_setup))					return setup_mgr;
		else if	(ctx.Match(k, Invk_scripts))				return gfs_mgr;
		else if	(ctx.Match(k, Invk_wiki_cfg_bldr))			return wiki_cfg_bldr;
		else if	(ctx.MatchPriv(k, Invk_term_cbk))			return this.Term_cbk();
		else if	(ctx.Match(k, Invk_xtns))					return xtn_mgr;
		else if	(ctx.Match(k, Invk_ctg_mgr))				return ctg_mgr;
		else if	(ctx.Match(k, Invk_cfgs))					return cfg_mgr;
		else if	(ctx.Match(k, Invk_usr_dlg))				return usr_dlg;
		else if	(ctx.Match(k, Invk_specials))				return special_mgr;
		else if	(ctx.Match(k, Invk_server))					return server;
		else if	(ctx.Match(k, Invk_app))					return this;  
		else if	(ctx.Match(k, Invk_fmtrs))					return fmtr_mgr;  
		else return GfoInvkAble_.Rv_unhandled;
	}
	public static final String Invk_gui = "gui", Invk_bldr = "bldr", Invk_wikis = "wikis", Invk_files = "files", Invk_langs = "langs", Invk_users = "users"
	, Invk_sys_cfg = "sys_cfg", Invk_url_aliases = "url_aliases", Invk_fsys = "fsys", Invk_cur = "cur", Invk_shell = "shell", Invk_log = "log"
	, Invk_setup = "setup", Invk_scripts = "scripts", Invk_wiki_cfg_bldr = "wiki_cfg_bldr", Invk_user = "user", Invk_xtns = "xtns", Invk_ctg_mgr = "ctg_mgr"
	, Invk_cfgs = "cfgs", Invk_app = "app", Invk_usr_dlg = "usr_dlg", Invk_specials = "specials", Invk_html = "html"
	, Invk_server = "server"
	, Invk_fmtrs = "fmtrs"
	;
	public static final String Invk_term_cbk = "term_cbk";
}
class Xoa_shell implements GfoInvkAble {
	public Xoa_shell(Xoa_app app) {this.app = app;} private Xoa_app app;
	public Object Invk(GfsCtx ctx, int ikey, String k, GfoMsg m) {
		if		(ctx.Match(k, Invk_fetch_page))				return String_.new_utf8_(app.Gui_mgr().Main_win().Exec_app_retrieve_by_url(m.ReadStr("url"), m.ReadStrOr("output_type", "html")));
		else if	(ctx.Match(k, Invk_chars_per_line_max_))	Chars_per_line_max_(m.ReadInt("v"));
		else return GfoInvkAble_.Rv_unhandled;
		return this;
	}
	private void Chars_per_line_max_(int v) {
		ConsoleAdp._.CharsPerLineMax_set(v);
	}
	private static final String Invk_fetch_page = "fetch_page", Invk_chars_per_line_max_ = "chars_per_line_max_";
}
