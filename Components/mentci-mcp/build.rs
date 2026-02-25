fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("../schema")
        .file("../schema/mcp.capnp")
        .run()
        .expect("schema compiler command");
}
