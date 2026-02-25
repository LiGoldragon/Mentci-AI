fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("../schema")
        .file("../schema/user.capnp")
        .run()
        .expect("schema compiler command");
}
