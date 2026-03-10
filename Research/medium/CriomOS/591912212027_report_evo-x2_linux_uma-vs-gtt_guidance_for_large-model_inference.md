# EVO-X2 Linux UMA vs GTT Guidance for Large-Model Inference

## Question
Should a GMKtec EVO-X2 / AMD Strix Halo Linux inference node set GPU memory allocation (UMA frame buffer / shared iGPU memory) to the maximum in firmware, leave it on Auto, or use a smaller manual value?

## Executive Answer
For a **Linux inference node** on Strix Halo, the best-supported guidance is **not** to max out the firmware UMA reservation. Instead:

1. enter UEFI and switch from `Auto` to **`UMA_SPECIFIED`**,
2. keep the **UMA Frame Buffer Size minimal** (commonly **512 MiB**), and
3. rely on Linux kernel/driver tuning (`amdgpu.gttsize`, `ttm.pages_limit`, related settings) so ROCm/llama.cpp/Ollama can use the much larger **GTT/shared-memory pool**.

This distinction matters:
- **UMA Frame Buffer** = fixed boot-time reservation for the iGPU
- **GTT / TTM-managed shared memory** = the large practical pool Linux inference actually uses

For this Linux workload, **max UMA is counterproductive**. It permanently removes large amounts of RAM from general system use while reducing the flexibility of the dynamic shared-memory approach used by current Strix Halo Linux inference guides.

## Source Findings

### 1. GMKtec official firmware guidance
GMKtec documents that the EVO-X2 firmware exposes the relevant setting under:
- `Advanced`
- `iGPU Configuration`
- set `UMA_SPECIFIED`
- then adjust `UMA Frame Buffer Size`

Their guide confirms the machine supports large manual reservations (for example, up to 96 GiB on 128 GiB systems), but this is only proof that the firmware allows it — not proof that Linux inference benefits from maxing it.

Source:
- https://www.gmktec.com/pages/evo-x2-bios-vram-size-adjustment-guide

### 2. pablo-ross Strix Halo Linux roadmap
The strongest Linux-specific practical guidance came from:
- `pablo-ross/strix-halo-gmktec-evo-x2`
- `ROADMAP.md`

That guidance recommends:
- **set GART/UMA to 512 MiB in firmware**
- disable IOMMU
- add kernel parameters for large GTT/shared memory
- do **not** try to change VRAM allocation from the OS afterward

It explicitly distinguishes:
- the small VRAM/framebuffer value visible at `/sys/class/drm/.../mem_info_vram_total`
- from the very large **GTT pool** visible at `mem_info_gtt_total`

The guide’s operational point is clear: on Linux, inference performance/feasibility comes from the large **GTT** pool, not from inflating the fixed firmware framebuffer reservation.

Source:
- https://raw.githubusercontent.com/pablo-ross/strix-halo-gmktec-evo-x2/main/ROADMAP.md

### 3. DeepWiki / Strix Halo toolbox guidance
A Linux-oriented Strix Halo reference summarized by DeepWiki also recommends:
- **512 MiB UMA Frame Buffer Size**
- then kernel settings so the GPU can see ~128 GiB of GTT/shared memory

It warns that larger firmware UMA reservations waste RAM and are not the mechanism that large-model Linux inference actually depends on.

Source:
- https://deepwiki.com/kyuz0/amd-strix-halo-toolboxes/6.3-host-system-configuration

### 4. Additional Linux caution about outdated parameters
Another Linux write-up warns that mismatched or deprecated memory parameters can misreport usable GPU memory. The key point is that Linux operators should focus on the **kernel/TTM/GTT configuration path**, not blindly on the firmware UMA number.

Source:
- https://dev.webonomic.nl/setting-up-unified-memory-for-strix-halo-correctly-on-ubuntu-25-04-or-25-10

### 5. Auto can work, but is less explicit
A separate Linux article shows that `Auto` UMA can still work if kernel tuning is correct, but that makes the result dependent on firmware defaults. For a reproducible inference appliance, explicit manual firmware settings are preferred.

Source:
- https://medium.com/@yjwong/running-deepseek-ocr-locally-on-amd-strix-halo-a-journey-into-local-ai-powered-document-processing-ed9ab4c77ed0

## Practical Recommendation
For `prometheus` as a dedicated Linux `largeAI` node:

### Firmware / UEFI
Use:
- `iGPU Configuration = UMA_SPECIFIED`
- `UMA Frame Buffer Size = 512 MiB` (or similarly minimal fixed value if firmware choices differ)

Do **not** set it to the maximum just because the firmware allows it.

### Linux / kernel side
Plan to configure the Linux inference stack around the documented Strix Halo shared-memory path, including the relevant `amdgpu` / `ttm` kernel settings so the machine exposes the large GTT/shared-memory pool to ROCm-style workloads.

### Why this is better for Linux
- preserves more RAM flexibility for the OS and page cache
- matches the Linux Strix Halo inference guidance already circulating
- keeps the framebuffer reservation small while still enabling large-model shared-memory execution
- avoids the Windows-style instinct of “max VRAM reservation” where Linux actually prefers dynamic shared memory

## Caveats
- Windows-oriented advice often pushes “max VRAM” because Windows lacks the same practical Linux GTT tuning path.
- Some Linux setups may appear to work on `Auto`, but that is less deterministic than explicitly setting a small fixed UMA and then tuning the kernel path.
- Exact kernel parameters should be validated against the current kernel/ROCm/Ollama lane we deploy on `prometheus`; older write-ups mention some deprecated options.

## Operator Guidance
If the immediate goal is “make this EVO-X2 useful for local large-model inference on Linux,” then the current evidence suggests:

1. Enter UEFI setup.
2. Change the iGPU mode from `Auto` to `UMA_SPECIFIED`.
3. Set the frame buffer reservation to a **small fixed value** (for example 512 MiB), not the maximum.
4. Reboot.
5. Finish the Linux-side tuning and verification for shared-memory/GTT-based inference.

## Bottom Line
For **Linux** on this SOC:
- **Auto** is acceptable but not ideal for repeatable tuning.
- **Maximum UMA** is **not** the recommended inference setting.
- **Manual small UMA + large GTT/TTM tuning** is the best-supported path for a dedicated large-model node.
