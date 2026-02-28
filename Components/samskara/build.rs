fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("schema")
        .file("schema/samskara_jj.capnp")
        .run()
        .expect("schema compilation failed");
}
