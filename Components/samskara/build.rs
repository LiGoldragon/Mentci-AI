fn main() {
    capnpc::CompilerCommand::new()
        .src_prefix("schema")
        .file("schema/samskara_editor.capnp")
        .run()
        .expect("compiling schema");
}
