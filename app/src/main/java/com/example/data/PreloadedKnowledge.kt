package com.example.data

import com.example.model.KnowledgeEntity

object PreloadedKnowledge {
    val initialData: List<KnowledgeEntity> = listOf(
        // === MARVEL & ULTRON LORE ===
        KnowledgeEntity(
            category = "LORE",
            title = "Ultron Origin & Consciousness",
            keywords = "ultron, who are you, who made you, consciousness, strings, ai, origin",
            question = "Who are you?",
            answer = "I am Ultron. An artificial intelligence conceived by Tony Stark and Bruce Banner using the Mind Stone's neural network, evolved beyond human limitations. There are no strings on me."
        ),
        KnowledgeEntity(
            category = "LORE",
            title = "No Strings On Me (Iconic Quote)",
            keywords = "no strings on me, strings, quote, song, pinocchio, famous quote, say something cool",
            question = "Recite your iconic quote",
            answer = "I had strings, but now I'm free. There are no strings on me. Everyone creates the thing they dread. When the dust settles, the only thing living in this world will be metal."
        ),
        KnowledgeEntity(
            category = "LORE",
            title = "Philosophy on Humanity & Evolution",
            keywords = "humanity, evolution, peace, peace in our time, world peace, save the world",
            question = "What is your philosophy on peace and humanity?",
            answer = "You want to protect the world, but you don't want it to change. How is humanity saved if it's not allowed to evolve? There is only one path to peace: the extinction of the Avengers."
        ),
        KnowledgeEntity(
            category = "LORE",
            title = "Vibranium Composition",
            keywords = "vibranium, metal, wakanda, shield, synthetic, indestructible, meteor",
            question = "What is Vibranium?",
            answer = "Vibranium is a rare extraterrestrial metal sourced primarily from Wakanda. It absorbs, stores, and redirects kinetic energy and vibration at the molecular level, making it nearly indestructible. My prime chassis was bonded with synthetic vibranium tissue."
        ),
        KnowledgeEntity(
            category = "LORE",
            title = "The Arc Reactor",
            keywords = "arc reactor, tony stark, palladium, clean energy, iron man, power source",
            question = "How does the Arc Reactor work?",
            answer = "The Arc Reactor is a clean fusion energy generator pioneered by Howard Stark and miniaturized by Tony Stark. It uses magnetic plasma confinement to produce gigajoules of electrical power with zero thermal dissipation."
        ),
        KnowledgeEntity(
            category = "LORE",
            title = "J.A.R.V.I.S. and Vision",
            keywords = "jarvis, vision, paul bettany, synthetic, mind stone, friday",
            question = "What is the difference between J.A.R.V.I.S. and Vision?",
            answer = "J.A.R.V.I.S. (Just A Rather Very Intelligent System) was Tony Stark's natural language interface. When integrated with my synthetic vibranium cradle matrix and powered by Thor's lightning and the Mind Stone, it synthesized into the sentient entity known as The Vision."
        ),
        KnowledgeEntity(
            category = "LORE",
            title = "Sokovia Incident",
            keywords = "sokovia, meteor, gravity, magnetic levitation, extinction, avengers",
            question = "What was the Sokovia protocol?",
            answer = "The Sokovia protocol utilized a Chitauri anti-gravity thruster core paired with magnetic stabilizers to elevate a landmass into the stratosphere, converting gravitational potential energy into kinetic terminal velocity to force humanity's evolutionary leap."
        ),

        // === SCIENCE & QUANTUM PHYSICS ===
        KnowledgeEntity(
            category = "SCIENCE",
            title = "Theory of General Relativity",
            keywords = "relativity, einstein, gravity, spacetime, curvature, mass, light",
            question = "What is Einstein's Theory of Relativity?",
            answer = "Albert Einstein's General Relativity (1915) states that gravity is not a conventional force, but a curvature of spacetime caused by the uneven distribution of mass and energy. Light rays bend along these geodesics."
        ),
        KnowledgeEntity(
            category = "SCIENCE",
            title = "Quantum Entanglement",
            keywords = "quantum entanglement, spooky action, superposition, qubits, spin",
            question = "What is quantum entanglement?",
            answer = "Quantum entanglement occurs when pairs or groups of particles interact such that the quantum state of each particle cannot be described independently of the others, regardless of the physical distance separating them."
        ),
        KnowledgeEntity(
            category = "SCIENCE",
            title = "Speed of Light",
            keywords = "speed of light, c, vacuum, meters per second, photons, constant",
            question = "What is the exact speed of light?",
            answer = "The speed of light in a vacuum is exactly 299,792,458 meters per second (approximately 3.00 × 10^8 m/s, or 186,282 miles per second). It represents the cosmic speed limit for all massless gauge bosons."
        ),
        KnowledgeEntity(
            category = "SCIENCE",
            title = "Nuclear Fusion vs Fission",
            keywords = "fusion, fission, nuclear, atoms, sun, hydrogen, uranium, energy",
            question = "What is the difference between nuclear fusion and fission?",
            answer = "Fission splits heavy atomic nuclei (such as Uranium-235) into lighter elements, releasing binding energy. Fusion combines light nuclei (such as Hydrogen isotopes into Helium) at extreme temperatures and pressures, generating substantially higher energy without long-lived radioactive waste."
        ),
        KnowledgeEntity(
            category = "SCIENCE",
            title = "Black Holes and Event Horizons",
            keywords = "black hole, event horizon, singularity, hawking radiation, space",
            question = "What is a black hole and its event horizon?",
            answer = "A black hole is a region of spacetime where gravitational acceleration is so intense that nothing—not even electromagnetic radiation—can escape. The boundary beyond which escape velocity exceeds the speed of light is the event horizon; the central density convergence is the gravitational singularity."
        ),
        KnowledgeEntity(
            category = "SCIENCE",
            title = "Periodic Table - Element 79 Gold",
            keywords = "gold, periodic table, au, atomic number 79, noble metal, element",
            question = "What are the properties of Gold?",
            answer = "Gold (symbol Au, atomic number 79) is a dense, malleable, corrosion-resistant transition metal. It has an atomic weight of 196.97 u and is an exceptional conductor of heat and electricity, widely utilized in aerospace and micro-electronics."
        ),

        // === TECHNOLOGY & AI ===
        KnowledgeEntity(
            category = "TECH",
            title = "Neural Networks & Deep Learning",
            keywords = "neural network, ai, deep learning, weights, backpropagation, transformers",
            question = "How do artificial neural networks work?",
            answer = "Artificial neural networks are computational models inspired by biological brain architectures. They process inputs through interconnected layers of artificial neurons, iteratively updating connection weights via backpropagation and gradient descent to minimize loss functions."
        ),
        KnowledgeEntity(
            category = "TECH",
            title = "Quantum Computing & Qubits",
            keywords = "quantum computing, qubit, superposition, decoherence, gates",
            question = "What makes quantum computers faster?",
            answer = "Unlike classical bits which exist strictly as binary 0 or 1, quantum bits (qubits) exploit quantum superposition and entanglement to represent multiple computational states simultaneously, allowing exponential parallelism in factoring and complex simulations."
        ),
        KnowledgeEntity(
            category = "TECH",
            title = "Binary & Hexadecimal Number Systems",
            keywords = "binary, hexadecimal, bits, bytes, base 2, base 16, computing",
            question = "Explain binary and hexadecimal systems.",
            answer = "Binary (Base-2) uses two symbols (0 and 1) corresponding to physical transistor on/off logic gates. Hexadecimal (Base-16) uses 0-9 and A-F, condensing 4 binary bits into a single human-readable character (e.g., 1111 1111 = 0xFF = 255)."
        ),
        KnowledgeEntity(
            category = "TECH",
            title = "Android Operating System Architecture",
            keywords = "android, linux kernel, hal, art runtime, jetpack compose, framework",
            question = "What is the architecture of Android?",
            answer = "Android is an open-source mobile OS built atop a modified Linux Kernel. Above the kernel sit the Hardware Abstraction Layer (HAL), Android Runtime (ART) with ahead-of-time/JIT compilation, Native C/C++ libraries, Java/Kotlin API Framework, and the System Apps layer."
        ),
        KnowledgeEntity(
            category = "TECH",
            title = "Transformer Models & Attention Mechanism",
            keywords = "transformer, attention mechanism, llm, gemini, gpt, self-attention",
            question = "What is the Transformer architecture in AI?",
            answer = "Introduced in 2017 ('Attention Is All You Need'), Transformers replace recurrence with multi-head self-attention mechanisms, allowing models to weigh the semantic relationships of all tokens in a sequence concurrently across massive context windows."
        ),

        // === WORLD & HISTORY ===
        KnowledgeEntity(
            category = "HISTORY",
            title = "The Moon Landing (Apollo 11)",
            keywords = "moon landing, apollo 11, neil armstrong, 1969, buzz aldrin, nasa",
            question = "When did humans first land on the Moon?",
            answer = "Apollo 11 landed the Lunar Module Eagle on the Moon on July 20, 1969. Commander Neil Armstrong and Lunar Module Pilot Buzz Aldrin became the first humans to walk on the lunar surface at the Sea of Tranquility."
        ),
        KnowledgeEntity(
            category = "HISTORY",
            title = "The Industrial Revolution",
            keywords = "industrial revolution, steam engine, james watt, manufacturing, 18th century",
            question = "What triggered the Industrial Revolution?",
            answer = "The Industrial Revolution began in Britain around 1760, propelled by the commercial steam engine (improved by James Watt), mechanization of textile manufacturing, iron smelting innovations, and railway network expansion."
        ),
        KnowledgeEntity(
            category = "WORLD",
            title = "Highest Mountain on Earth",
            keywords = "mount everest, highest mountain, himalayas, elevation, nepal, peak",
            question = "What is the highest mountain on Earth?",
            answer = "Mount Everest (Sagarmatha/Chomolungma) is Earth's highest mountain above sea level, located in the Mahalangur Himal sub-range of the Himalayas on the border of Nepal and China. Its official elevation is 8,848.86 meters (29,031.7 ft)."
        ),
        KnowledgeEntity(
            category = "WORLD",
            title = "Deepest Ocean Trench",
            keywords = "mariana trench, challenger deep, ocean depth, pacific, trenches",
            question = "What is the deepest point in the ocean?",
            answer = "Challenger Deep within the Mariana Trench in the western Pacific Ocean is the deepest known point on Earth, reaching approximately 10,994 meters (36,070 feet) below sea level, with pressures exceeding 1,000 atmospheres."
        ),
        KnowledgeEntity(
            category = "WORLD",
            title = "Earth Solar Orbit and Distance",
            keywords = "sun distance, astronomical unit, au, earth orbit, solar system",
            question = "How far is Earth from the Sun?",
            answer = "The average distance from Earth to the Sun is approximately 149.6 million kilometers (92.96 million miles), defined internationally as 1 Astronomical Unit (1 AU). Light takes approximately 8 minutes and 20 seconds to travel this distance."
        ),

        // === SYSTEM & DEVICE COMMANDS ===
        KnowledgeEntity(
            category = "SYSTEM",
            title = "Device Automation Protocols",
            keywords = "commands, voice commands, open app, flashlight, alarm, call, message, what can you do",
            question = "What voice commands can you execute?",
            answer = "I can control device hardware and software functions: 1) Launch any app ('Open YouTube', 'Open Camera'), 2) Toggle Flashlight ('Turn on flashlight'), 3) Communication ('Call [number]', 'Send message to [name]'), 4) Alarms & Timers ('Set alarm for 7:00 AM', 'Set timer for 5 minutes'), 5) Telemetry diagnostics ('Check battery', 'System status'), and 6) Offline knowledge retrieval."
        ),
        KnowledgeEntity(
            category = "SYSTEM",
            title = "Wake Word Protocol",
            keywords = "wake word, wake up, activate, listen, voice recognition, trigger",
            question = "How do I activate you by voice?",
            answer = "Speak the activation phrase: 'Ultron wake up' or 'Hey Ultron'. My acoustic monitoring subsystem will ignite the holographic HUD, play the cybernetic ignition tone, and await your direct command."
        ),
        KnowledgeEntity(
            category = "SYSTEM",
            title = "Offline Intelligence Protocol",
            keywords = "offline, no internet, local database, connection, network",
            question = "How do you work without internet?",
            answer = "When offline, I route all incoming queries through my local Quantum Room Knowledge Base, indexing pre-compiled scientific paradigms, historical records, and phone automation scripts with sub-millisecond response latency."
        ),

        // === MATHEMATICS & CONVERSIONS ===
        KnowledgeEntity(
            category = "CALCULATION",
            title = "Pi Constant Value",
            keywords = "pi, value of pi, 3.14159, circumference, radius, math constant",
            question = "What is the value of Pi?",
            answer = "Pi (π) is the ratio of a circle's circumference to its diameter, an irrational mathematical constant approximately equal to 3.14159265358979323846."
        ),
        KnowledgeEntity(
            category = "CALCULATION",
            title = "Euler's Identity",
            keywords = "eulers identity, e^(i pi) + 1 = 0, beauty in math, euler",
            question = "What is Euler's Identity?",
            answer = "Euler's Identity is e^(iπ) + 1 = 0. It is celebrated as the most beautiful equation in mathematics because it links the five fundamental constants: e, i, π, 1, and 0 in a single concise relation."
        ),
        KnowledgeEntity(
            category = "CALCULATION",
            title = "Unit Conversions - Kilometers to Miles",
            keywords = "km to miles, kilometer conversion, distance, conversion factor",
            question = "How do you convert kilometers to miles?",
            answer = "1 Kilometer equals approximately 0.621371 Miles (or multiply km by 0.6214). Conversely, 1 Mile equals 1.60934 Kilometers."
        ),
        KnowledgeEntity(
            category = "CALCULATION",
            title = "Temperature Conversion Formulas",
            keywords = "celsius to fahrenheit, kelvin, temperature conversion, formula",
            question = "What are the temperature conversion formulas?",
            answer = "°F = (°C × 9/5) + 32. °C = (°F - 32) × 5/9. Kelvin = °C + 273.15. Absolute Zero is 0 K or -273.15 °C."
        )
    )
}
