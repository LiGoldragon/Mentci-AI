fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("schema")
        .file("schema/stt.capnp")
        .run()
        .expect("schema compiler command");
}
