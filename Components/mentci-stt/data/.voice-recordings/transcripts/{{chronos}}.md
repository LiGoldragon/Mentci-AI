Okay.

So, um, each component that needs to talk to another component has a contract, um, is a very intricate contract uh, upkeep system. So it's all specified in Cap'n Proto, of course.

And uh...
[sighs]
So, component, okay, so let's take an example from the project.

So say...
Here, let me look.
[pause]
Okay.
[long pause]
Yeah, so, let's say Mentci Intel, which is uh, the new component drafted for uh, essentially storing and loading the the... essentially the *memory*, which is now standardized as markdown files, but the memory of the agent.

So, um, Mentci Intel needs to talk to whatever is going to launch the agent, right? So, we probably, we should have a component for that.

So Mentci Agent, which is going to be this component that's going to be made now.

And um...
[pause]
So, Mentci in this case, Mentci Intel, and this is the pattern, is gonna have a uh, several directories. Let's say it's going— we we can standardize this in the file system, so it's easy to keep track of. Um, and because Mentci uses its own custom build system, we can do whatever we want. So, on the top, top level, we could have a directory where all of these, I guess, *specifications*, or, yeah, can live. And then inside there is going to be its own specification, Mentci Intel. And then there's gonna be another directory called Mentci, or, but it doesn't have to be called Mentci Intel, because we're already in the context of Mentci. So it's just Intel. And maybe, um, maybe all these directories—but this is a bigger job—but maybe all these Mentci prefixes could be should be taken out of the components.
[pause]
But maybe it's giving their type. Yeah. Yeah, yeah, it's giving their type. So it's a Mentci type. Okay, so leave it there.
[sighs]
It's information. Until we separate the components into further sub-directories, but we could just keep it a flattening space for now. And uh, so Mentci Intel, and then there's another, it's gonna have another specification called Mentci Agent for the this upcoming Mentci Agent component.

And then inside there,
[pause]
uh, or,
[pause]
yeah. Either it's—either it's all embedded in one tree or there's another directory top level that's uh *proposals*. Maybe all these live under contracts. So,
[pause]
yeah, maybe *contracts* is better than specifications. I think so. Yeah. So, **contracts** is—it's not called specifications, it's called contracts. And then there's current and proposed. Yeah. Yeah. So, the one it knows to be current. And then in the current, uh,
[pause]
contract, the um,
[long pause]
Oh, **wow**!
[pause]
So it's another component.
[pause]
It's another repository. The contract, the current contract of a component and the and the proposed contract of a component is yet *another* repository, because if we want to make the Unix file system atomic, we have to use version control systems, which is Jojutsu, JJ.

So we make another repository. So components will have subtrees, of course, and that's one of them is their contracts. So, all of the different versions, the current contract, and the proposed contract. And then it's gonna have a history of past contract versions.

So one one side can work, when when an agent works on Mentci Agent, for example, and needs to to update the Mentci, um, Intel contract, that it shares with Mentci Intel, which is specific to Mentci Agent talking to Mentci Intel. Then it's going to update that into the proposal and push it to a branch that then gets picked up when this is all like continuous, right? This is gonna be a continuous machine. So it gets picked—it gets picked up by Mentci uh, Intel hook that there's a new proposal for the contract with Mentci Agent. So then, uh, the proper route would be for Mentci—for another agent to spin up from the job controller, which manages available computation and does—talks to the accountant, uh, uh, obviously, to see if this is important enough to update now or, you know, if it's a deep research project that is sort of uh, backburner project in terms of importance of what needs to be done soon. Like there's something urgent that has to happen for it, it might not trigger a build for it to test the new proposal, but if it's **critical**, then it's probably gonna start it right away. So that before even the uh job is done on changing Mentci Agent, Mentci Intel could have tested the new contract and built and rebuilt and then trigger the rebuild for anything that depends on Mentci Intel to rebuild as well, if necessary. Maybe there's a way even around that.

Because if we created even another binary, a node between uh components that can route messages, because the contract hasn't been updated between Mentci Agent—if this would be implemented—between Mentci Agent and the other things that talk to it, then it might not need to update or rebuild the other stuff, because the uh the component that talks to Mentci Agent doesn't need to change because the message that Mentci Agent uses to talk to that sort of universal controller would not have changed. So that that could be a way to uh decentralize build logic in the future to avoid rest rebuilds. And it might actually be worth it.
[pause]
But uh, yeah.
[pause]
The the *cool* thing actually is that's already done by making the the contract, the component, a different repo. Because since—oh, **wow**! I didn't even realize that! The node is the contract!
**Wow!** **Oh my god**, I didn't even see the genius the first time!

So yeah, do a full uh, project-wide implementation research and then eventually probably will—yeah.
[pause]
Yeah, the agent will figure out what it wants to do. If it wants to run another pass for this, it's probably a multi-pass thing because we don't have multi-pass built into Mentci-AI yet, or probably not by the time this audio is being run. Um, but Gemini's still working right now.
[pause]
Build to verify. Okay, it's verifying the build, so,
[pause]
should be done soon.
[pause]
So yeah, that's the architecture for um,
[pause]
but I don't know, like this is a *big* change, right? So like we need to implement this uh, version control sub-tree system. It's obviously a high-level orchestration thing, um, that's that spawns above Mentci-AI, *really*. So we could create a repository that has Mentci-AI as one of its subtrees, and um,
[long pause]
in a sense, Nix is the storage logic for now. So we don't need to restore it. Nix is already storing in terms of um, content addressing all all of this. And knowing when a rebuild like, you know, because something changes, then a rebuild is necessary. Um,
[pause]
yeah, so we just put everything on GitHub for now until we're eventually gonna run our own Git servers, obviously. But uh, or it might be, yeah, um, there's this uh, oh yeah, so here's the protocol for audio files because they're usually gonna be broad, right? There can be many subjects. So first a pass is gonna be made to with that file to separate out all of the different, um, organize those thoughts into actionable uh, development reports and research reports, R&D. And that's uh, yeah, another system that needs to be better integrated into the agent so that uh the prompt—the agent doesn't have to read so much. It should probably just start running commands almost, because uh or if it needs to, or editing stuff. As soon I the—its first action shouldn't be to try and figure out if it needs more prompt, basically, which is a lot of what happens right now is the agent looks for agent.md file and it's like we've gone *past* that. We're gone into the smart agent now, so it knows because it's receiving contexts from another agent. So it's an infinite loop of context passing events.
[pause]
Um,
[pause]
which are happening through this Cap'n Proto messaging system.

So even the message changes the default message when we update the contract. Uh, there's a pre-compiled message that will probably be—you might as well commit it because it's a change anyway. So put it in with that change uh because the the the previous binary message won't work anyway. So that that that's part like of of the hard constraints of changing these specifications, it's gonna be the agents are gonna get more and more specialized and constrained as to what outcome can actually come out of them. So for for changing the specification, probably a really simple, fast, uh, language model will be used and it has only a few things—the outcomes that can be allowed like the and the tests have to pass, right? So the message has to actually read correctly, which is what the test harness is gonna do. So there's a part of the intelligence that gets automated. Kind of like how the muscle of the human physical and energetic body's the configuration, the ligaments and everything forces a certain way of doing things. Like we we front—we we face things to deal with them usually because our hands face forward, our eyes face forward, our nose face forward, our mouth face forward, our sexual uh, organs face forward. So the machine is gonna be the same. Its components are gonna be organized in a certain pattern. It's like the knife has a final shape and the computer has a final shape. It's inevitable. It just has sort of infinite little variants that makes it, um, because it evolves with its psyche users. So there's as much as there's different psychies, then there's going to be different styles of machine to to help these psychies to achieve their Dharma.