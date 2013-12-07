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
package gplx.xowa.servers; import gplx.*; import gplx.xowa.*;
import gplx.ios.*; import gplx.json.*;
public class Xosrv_server implements GfoInvkAble {
	private long last_cmd;
	public Xosrv_socket_rdr Rdr() {return rdr;} private Xosrv_socket_rdr rdr = new Xosrv_socket_rdr();
	public Xosrv_socket_wtr Wtr() {return wtr;} private Xosrv_socket_wtr wtr = new Xosrv_socket_wtr();
	public int Rdr_port() {return rdr_port;} public Xosrv_server Rdr_port_(int v) {rdr_port = v; return this;} private int rdr_port = 55000;
	public int Wtr_port() {return wtr_port;} public Xosrv_server Wtr_port_(int v) {wtr_port = v; return this;} private int wtr_port = 55001;
	public int Shutdown_interval() {return shutdown_interval;} public Xosrv_server Shutdown_interval_(int v) {shutdown_interval = v; return this;} private int shutdown_interval = -1;
	public String Wtr_host() {return wtr_host;} private String wtr_host = "localhost";
	public boolean Running() {return running;} public Xosrv_server Running_(boolean v) {running = v; running_str = Bool_.XtoStr_lower(running); return this;} private boolean running = false;
	public String Running_str() {return running_str;} String running_str = "false";
	public void App_ctor(Xoa_app app) {this.app = app;}
	public Xoa_app App() {return app;} private Xoa_app app;
	private Gxw_html_server html_box;
	public void Run() {
		rdr.Init(this, rdr_port);
		wtr.Init(wtr_host, wtr_port);

		html_box = new Gxw_html_server(this);
		Gfo_usr_dlg_ui usr_dlg_ui = app.Usr_dlg().Ui_wkr();
		app.Gui_mgr().Kit_(gplx.gfui.Gfui_kit_base._.Tid_(gplx.gfui.Gfui_kit_.TypeId_swt));
		app.Usr_dlg().Ui_wkr_(usr_dlg_ui);
		app.Gui_mgr().Main_win().Html_box().Under_html_(html_box);

		app.Webserver().App_ctor(app);
		//app.Webserver().Run();	//Start the Webserver
		new Thread(app.Webserver()).start();
		app.Usr_dlg().Note_many("", "", "Webserver started: listening on 8080.");
		
		ThreadAdp_.invk_(rdr, Xosrv_socket_rdr.Invk_start).Start();
		app.Usr_dlg().Note_many("", "", "server started: listening on ~{0}. Press Ctrl+C to exit", rdr_port);
		last_cmd = Env_.TickCount();
		Running_(true);
		while (running) {
			if (shutdown_interval != -1 && Env_.TickCount() - last_cmd > shutdown_interval) break;
			ThreadAdp_.Sleep(1000);
		}
		rdr.Rls();
		wtr.Rls();
		app.Usr_dlg().Note_many("", "", "server stopped", rdr_port);
	}
	public void Msg_rcvd(Xosrv_msg msg) {
		try {
			byte[] cmd_name = msg.Cmd_name();
			byte[] rsp_name = ByteAry_.Empty;
			long time_bgn = Env_.TickCount();
			last_cmd = time_bgn;
			byte[] msg_bry = msg.Msg_text();
			String msg_str = String_.new_utf8_(msg_bry);
			app.Usr_dlg().Note_many("", "", "processing cmd: ~{0}", msg_str);
			String rsp_str = null;
			if		(ByteAry_.Eq(cmd_name, Xosrv_cmd_types.Cmd_exec)) 	{rsp_name = Xosrv_cmd_types.Cmd_pass; rsp_str = Exec_cmd(msg_str);}
			else if	(ByteAry_.Eq(cmd_name, Xosrv_cmd_types.Js_exec)) 	{rsp_name = Xosrv_cmd_types.Js_pass;  rsp_str = Exec_js(msg.Sender(), msg_bry);}
			Xosrv_msg rsp_msg = Xosrv_msg.new_(rsp_name, msg.Msg_id(), msg.Recipient(), msg.Sender(), msg.Msg_date(), ByteAry_.new_utf8_(rsp_str));
			app.Usr_dlg().Note_many("", "", "sending rsp: bytes=~{0}", String_.Len(rsp_str));
			wtr.Write(rsp_msg);		
			app.Usr_dlg().Note_many("", "", "rsp sent: elapsed=~{0}", TimeSpanAdp_.fracs_(Env_.TickCount() - time_bgn).XtoStrUiAbbrv());
		} catch (Exception e) {app.Usr_dlg().Warn_many("", "", "server error: ~{0}", Err_.Message_gplx(e));}
	}
	private String Exec_cmd(String msg_text) {
		return Object_.XtoStr_OrNull(app.Gfs_mgr().Run_str(msg_text));
	}
	public String Exec_js(byte[] sender, byte[] msg_text) {
		StringRef trace = StringRef.new_("exec_js");
		try {
			Object[] xowa_exec_args = xowa_exec_parser.Parse_xowa_exec(msg_text);
			trace.Val_("js_args");
//				xowa_exec_args = (Object[])Array_.Resize(xowa_exec_args, xowa_exec_args.length + 1);
//				xowa_exec_args[xowa_exec_args.length - 1] = sender;
			Object rv_obj = gplx.gfui.Gfui_html.Js_args_exec(app.Gui_mgr().Main_win().Js_cbk(), xowa_exec_args);
			trace.Val_("json_write: " + Object_.XtoStr_OrNullStr(rv_obj));
			return json_wtr.Write_root(Bry_xowa_js_result, rv_obj).Bld_as_str();
		} catch (Exception e) {throw Err_.err_(e, "exec_js error: {0} {1} {2}", trace, msg_text, Err_.Message_gplx(e));}
	}	private Xosrv_xowa_exec_parser xowa_exec_parser = new Xosrv_xowa_exec_parser(); private Json_doc_srl json_wtr = new Json_doc_srl(); private static final byte[] Bry_xowa_js_result = ByteAry_.new_ascii_("xowa_js_result");
	public Object Invk(GfsCtx ctx, int ikey, String k, GfoMsg m) {
		if		(ctx.Match(k, Invk_rdr_port))				return rdr_port;
		else if	(ctx.Match(k, Invk_rdr_port_))				rdr_port = m.ReadInt("v");
		else if	(ctx.Match(k, Invk_wtr_port))				return wtr_port;
		else if	(ctx.Match(k, Invk_wtr_port_))				wtr_port = m.ReadInt("v");
		else if	(ctx.Match(k, Invk_wtr_host))				return wtr_host;
		else if	(ctx.Match(k, Invk_wtr_host_))				wtr_host = m.ReadStr("v");
		else if	(ctx.Match(k, Invk_shutdown_interval))		return shutdown_interval;
		else if	(ctx.Match(k, Invk_shutdown_interval_))		shutdown_interval = m.ReadInt("v");
		else if	(ctx.Match(k, Invk_stop))					running = false;
		else	return GfoInvkAble_.Rv_unhandled;
		return this;
	}
	public static final String Invk_stop = "stop", Invk_rdr_port = "rdr_port", Invk_rdr_port_ = "rdr_port_", Invk_wtr_port = "wtr_port", Invk_wtr_port_ = "wtr_port_", Invk_wtr_host = "wtr_host", Invk_wtr_host_ = "wtr_host_"
	, Invk_shutdown_interval = "shutdown_interval", Invk_shutdown_interval_ = "shutdown_interval_";
}
class Xosrv_cmd_types {
	public static final byte[]
		Cmd_exec 		= ByteAry_.new_ascii_("xowa.cmd.exec")	, Cmd_pass	 	= ByteAry_.new_ascii_("xowa.cmd.result")	, Cmd_fail		= ByteAry_.new_ascii_("xowa.cmd.error")
	,	Js_exec 		= ByteAry_.new_ascii_("xowa.js.exec")	, Js_pass		= ByteAry_.new_ascii_("xowa.js.result")		, Js_fail		= ByteAry_.new_ascii_("xowa.js.error")
	,	Browser_exec 	= ByteAry_.new_ascii_("browser.js.exec"), Browser_pass 	= ByteAry_.new_ascii_("browser.js.result")	, Browser_fail 	= ByteAry_.new_ascii_("browser.js.error")
	;
}
class Xosrv_xowa_exec_parser {
	private Json_parser json_parser = new Json_parser();
	public Object[] Parse_xowa_exec(byte[] msg_text) {	// parses JSON with format '{"args":["arg0","arg1","arg2"]}'
		Json_doc doc = json_parser.Parse(msg_text);
		Json_itm_kv args_kv = (Json_itm_kv)doc.Root().Subs_get_at(0);	// get "args" kv
		Json_itm_ary args_ary = (Json_itm_ary)args_kv.Val();			// get []
		int len = args_ary.Subs_len();			
		Object[] rv = new Object[len];
		for (int i = 0; i < len; i++) {	// extract args
			Json_itm itm = args_ary.Subs_get_at(i);
			rv[i] = Parse_ary_itm(itm);
		}
		return rv;
	}
	private Object Parse_ary_itm(Json_itm itm) {
		switch (itm.Tid()) {
			case Json_itm_.Tid_string:
				return String_.new_utf8_(itm.Data_bry());
			case Json_itm_.Tid_array: 
				Json_itm_ary ary = (Json_itm_ary)itm;
				int len = ary.Subs_len();
				String[] rv = new String[len];
				for (int i = 0; i < len; i++)
					rv[i] = String_.new_utf8_(ary.Subs_get_at(i).Data_bry());
				return rv;
			default:
				throw Err_.unhandled(itm.Tid());
		}
	}
}
