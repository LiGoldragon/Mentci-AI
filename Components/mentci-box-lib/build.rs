fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("../schema")
        .file("../schema/mentci_box.capnp")
        .run()
        .expect("capnp compilation failed");
}
